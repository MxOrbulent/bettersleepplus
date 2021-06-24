package net.alphasucks.bettersleepplus;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.alphasucks.bettersleepplus.config.BSPConfig;
import net.alphasucks.bettersleepplus.config.ConfigHandeler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.level.ServerWorldProperties;

import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BetterSleepPlus implements ModInitializer {

    public static final int RAIN_AWAKE_TIME = 23992;
    public static final int THUNDER_AWAKE_TIME = 23460;
    public static final long DAYLENGTH = 24000L;
    private static final DynamicCommandExceptionType floatFormat = new DynamicCommandExceptionType(o -> new LiteralText("§c" + o + " is out of the required range. The value needs to be between 0 and 1"));
    public static ConfigHandeler config;
    public static boolean toUpdate = true;

    public static void customSleepTick(int players, int sleepingPlayers, ServerWorld instance, List<ServerPlayerEntity> serverPlayerEntities, ServerWorldProperties worldProperties) {
        CommandBossBar bossBar = initBossbar(instance);
        if (players >= 1) {
            if (sleepingPlayers >= 1) {
                int estimate = tickTime(worldProperties, instance, (float) sleepingPlayers / (float) players);
                tickText(instance, serverPlayerEntities, bossBar, worldProperties, estimate, players, sleepingPlayers);
            } else {
                bossBar.setVisible(false);
            }
        }
    }

    private static void tickText(ServerWorld instance, List<ServerPlayerEntity> serverPlayerEntities, CommandBossBar commandBossBar, ServerWorldProperties worldProperties, int estimate, int players, int playersSleeping) {
        commandBossBar.addPlayers(serverPlayerEntities);
        final BSPConfig config = BetterSleepPlus.config.getValues();
        final long timeOfDay = instance.getTimeOfDay() % DAYLENGTH;
        String commandBossBartext;
        if (!((worldProperties.getThunderTime() > 60) && worldProperties.isThundering())) {
            final int startOfSleep = instance.isRaining() ? 12010 : 12542;
            final int endOfSleep = instance.isRaining() ? RAIN_AWAKE_TIME : THUNDER_AWAKE_TIME;
            commandBossBar.setValue((int) ((((double) timeOfDay - startOfSleep) / (endOfSleep - startOfSleep) * 1000)));
            commandBossBartext = config.progressNightText;
        } else {
            commandBossBar.setValue((int) ((((double) timeOfDay / DAYLENGTH * 1000))));
            commandBossBartext = config.progressLightningText;
        }


        //set bossbar color
        if ((12000 < timeOfDay && timeOfDay < 13000) || (23000 < timeOfDay && timeOfDay < DAYLENGTH - 1)) {
            commandBossBar.setColor(BossBar.Color.YELLOW);
        } else if (13000 < timeOfDay && timeOfDay < 23000) {
            commandBossBar.setColor(BossBar.Color.PURPLE);
        } else if (0 < timeOfDay && timeOfDay < 12000) {
            commandBossBar.setColor(BossBar.Color.BLUE);
        }

        commandBossBar.setName(getFancyString(commandBossBartext, estimate, playersSleeping, players));
        MutableText actionText = getFancyString(config.infoText, estimate, playersSleeping, players);
        for (ServerPlayerEntity player : serverPlayerEntities) {
            player.sendMessage(actionText, true);
        }
        commandBossBar.setVisible(true);
    }


    private static int tickTime(ServerWorldProperties worldProperties, ServerWorld server, float playerDecimal) {
        final BSPConfig config = BetterSleepPlus.config.getValues();
        final double curve = config.curveAggression;
        final float start = config.curveStart;
        final double v = (1 / (config.curveStop - start)) * (playerDecimal - start);
        int timeStep = Math.round(
                playerDecimal > Math.min(config.curveCutoffStop, config.curveStop) ?
                        1 : (float) ((playerDecimal < Math.max(config.curveCutoffStart, config.curveStart) ?
                        0 : config.curveStart + ((config.curveMaxSpeed * (curve * playerDecimal / (2 * curve * playerDecimal - curve - playerDecimal + 1))) * v)
                ))
        );
        worldProperties.setTimeOfDay(worldProperties.getTimeOfDay() + (long) timeStep);

        worldProperties.setRainTime(timeOverstepCheck(worldProperties.getRainTime(), timeStep));
        worldProperties.setThunderTime(timeOverstepCheck(worldProperties.getThunderTime(), timeStep));
        worldProperties.setClearWeatherTime(timeOverstepCheck(worldProperties.getClearWeatherTime(), timeStep));
        server.getServer().getPlayerManager().sendToDimension(new WorldTimeUpdateS2CPacket(server.getTime(), server.getTimeOfDay(), server.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)), server.getRegistryKey());
        long timeOfDay = server.getTimeOfDay() % DAYLENGTH;
        if (!((worldProperties.getThunderTime() > 60) && worldProperties.isThundering())) {
            return (int) ((Math.round((((server.isRaining() ? RAIN_AWAKE_TIME : THUNDER_AWAKE_TIME) - timeOfDay) / ((timeStep + 1) * 20d)) * 10)) / 10d);
        } else {
            return (int) ((Math.round(((Math.min(worldProperties.getThunderTime(), worldProperties.getRainTime()) - 60) / ((timeStep + 1) * 20d)) * 10)) / 10d);
        }

    }

    private static int timeOverstepCheck(int currentTime, int timeStep) {
        final int finalTime = currentTime - timeStep;
        return finalTime > timeStep ? currentTime : finalTime;
    }


    private static CommandBossBar initBossbar(ServerWorld server) {
        final Identifier bossid = new Identifier("bettersleepplus:bossbar");
        BossBarManager bossBarManager = server.getServer().getBossBarManager();
        CommandBossBar commandBossBar = bossBarManager.get(bossid);
        if (!(bossBarManager.getIds().contains(bossid))) {
            commandBossBar = bossBarManager.add(bossid, new LiteralText("sleep"));
            commandBossBar.setMaxValue(1000);
            commandBossBar.setColor(BossBar.Color.BLUE);
        }
        return commandBossBar;
    }

    public static MutableText getFancyString(String string, long seconds, int sleepingPlayers, int players) {
        String playerDecimalColor = switch (Math.round(((float) sleepingPlayers / (float) players) * 5)) {
            case 0, 1 -> Formatting.RED.toString();
            case 2, 3, 4 -> Formatting.YELLOW.toString();
            case 5 -> Formatting.GREEN.toString();
            default -> Formatting.DARK_RED.toString();
        };
        return new LiteralText(string
                .replace("<countdown>", (((seconds / 3600) < 10 ? "0" + (seconds / 3600) : (seconds / 3600)) + ":" + ((seconds / 60) < 10 ? "0" + (seconds / 60) : (seconds / 60)) + ":" + ((seconds % 60) < 10 ? "0" + (seconds % 60) : (seconds % 60))))
                .replace("<seconds>", String.valueOf(seconds))
                .replace("<minutes>", String.valueOf(seconds / 60))
                .replace("<hours>", String.valueOf(seconds / 3600))
                .replace("<sleepingcolor>", playerDecimalColor)
                .replace("<playerssleeping>", String.valueOf(sleepingPlayers))
                .replace("<players>", String.valueOf(players)));
    }

    private static String processString(String st) {
        return st.replace('&', '§');
    }


    /**
     * <h2>
     * “According to all known laws of aviation, there is no way a bee should be able to fly.
     * It's wings are too small to get its fat little body off the ground.
     * The bee, of course, flies anyway, because bees don't care what humans think is impossible.”
     * </h2>
     */
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            final LiteralArgumentBuilder<ServerCommandSource> bettersleepplus = literal("bettersleepplus");
            dispatcher.register(bettersleepplus
                    .then(literal("reload").executes(context -> {
                        context.getSource().sendFeedback(Text.of("Reloaded BetterSleepPlus Config."), true);
                        BetterSleepPlus.toUpdate = true;
                        return 0;
                    }))
                    .then(literal("resetValues").executes(context -> {
                        context.getSource().sendFeedback(Text.of("Reset all values to default."), true);
                        ConfigHandeler.instance.config = new BSPConfig();
                        ConfigHandeler.instance.updateConfig();
                        return 0;
                    }))
                    .then(literal("setValue")
                            .then(literal("progressNightText").then(argument("string", StringArgumentType.string()).executes(context -> {
                                final String string = processString(StringArgumentType.getString(context, "string"));
                                context.getSource().sendFeedback(Text.of("Changed Sleep Text to: " + string), true);
                                ConfigHandeler.instance.config.progressNightText = string;
                                ConfigHandeler.instance.updateConfig();
                                return 0;
                            })))
                            .then(literal("progressLightningText").then(argument("string", StringArgumentType.string()).executes(context -> {
                                final String string = processString(StringArgumentType.getString(context, "string"));
                                context.getSource().sendFeedback(Text.of("Changed Lightning Sleep Text to: " + string), true);
                                ConfigHandeler.instance.config.progressLightningText = string;
                                ConfigHandeler.instance.updateConfig();
                                return 0;
                            })))
                            .then(literal("infoText").then(argument("string", StringArgumentType.string()).executes(context -> {
                                final String string = processString(StringArgumentType.getString(context, "string"));
                                context.getSource().sendFeedback(Text.of("Changed ActionBar Text to: " + string), true);
                                ConfigHandeler.instance.config.infoText = string;
                                ConfigHandeler.instance.updateConfig();
                                return 0;
                            })))
                            .then(literal("maxTicksAdded").then(argument("int", IntegerArgumentType.integer()).executes(context -> {
                                final int anInt = IntegerArgumentType.getInteger(context, "int");
                                context.getSource().sendFeedback(Text.of("Changed maxTicksAdded to: " + anInt), true);
                                ConfigHandeler.instance.config.curveMaxSpeed = anInt;
                                ConfigHandeler.instance.updateConfig();
                                return 0;
                            })))
                            .then(literal("curveAggression").then(argument("float", FloatArgumentType.floatArg()).executes(context -> {
                                final float aFloat = FloatArgumentType.getFloat(context, "float");
                                if (!(aFloat > 0 && aFloat < 1)) {
                                    throw floatFormat.create(aFloat);
                                }
                                context.getSource().sendFeedback(Text.of("Changed curveAggression to: " + aFloat), true);
                                ConfigHandeler.instance.config.curveAggression = aFloat;
                                ConfigHandeler.instance.updateConfig();
                                return 0;
                            })))
                            .then(literal("curveCutoffStart").then(argument("float", FloatArgumentType.floatArg()).executes(context -> {
                                final float aFloat = FloatArgumentType.getFloat(context, "float");
                                if (!(aFloat > 0 && aFloat < 1)) {
                                    throw floatFormat.create(aFloat);
                                }
                                context.getSource().sendFeedback(Text.of("Changed curveCutoffStart to: " + aFloat), true);
                                ConfigHandeler.instance.config.curveCutoffStart = aFloat;
                                ConfigHandeler.instance.updateConfig();
                                return 0;
                            })))
                            .then(literal("curveCutoffStop").then(argument("float", FloatArgumentType.floatArg()).executes(context -> {
                                final float aFloat = FloatArgumentType.getFloat(context, "float");
                                if (!(aFloat > 0 && aFloat < 1)) {
                                    throw floatFormat.create(aFloat);
                                }
                                context.getSource().sendFeedback(Text.of("Changed curveCutoffStop to: " + aFloat), true);
                                ConfigHandeler.instance.config.curveCutoffStop = aFloat;
                                ConfigHandeler.instance.updateConfig();
                                return 0;
                            })))
                            .then(literal("curveStart").then(argument("float", FloatArgumentType.floatArg()).executes(context -> {
                                final float aFloat = FloatArgumentType.getFloat(context, "float");
                                if (!(aFloat > 0 && aFloat < 1)) {
                                    throw floatFormat.create(aFloat);
                                }
                                context.getSource().sendFeedback(Text.of("Changed curveStart to: " + aFloat), true);
                                ConfigHandeler.instance.config.curveStart = aFloat;
                                ConfigHandeler.instance.updateConfig();
                                return 0;
                            })))
                            .then(literal("curveStop").then(argument("float", FloatArgumentType.floatArg()).executes(context -> {
                                final float aFloat = FloatArgumentType.getFloat(context, "float");
                                if (!(aFloat > 0 && aFloat < 1)) {
                                    throw floatFormat.create(aFloat);
                                }
                                context.getSource().sendFeedback(Text.of("Changed curveStop to: " + aFloat), true);
                                ConfigHandeler.instance.config.curveStop = aFloat;
                                ConfigHandeler.instance.updateConfig();
                                return 0;
                            })))
                            .then(literal("showProgressbar").then(argument("boolean", BoolArgumentType.bool()).executes(context -> {
                                final boolean aBoolean = BoolArgumentType.getBool(context, "boolean");
                                context.getSource().sendFeedback(Text.of("Changed progressBar visibility to: " + aBoolean), true);
                                ConfigHandeler.instance.config.showProgressbar = aBoolean;
                                ConfigHandeler.instance.updateConfig();
                                return 0;
                            })))
                    )
            );
        });
        //celeste mountain ^^^
    }
}
