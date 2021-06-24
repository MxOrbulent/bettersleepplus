package net.alphasucks.bettersleepplus.config;


public class BSPConfig {

    public String progressNightText;
    public String progressLightningText;
    public String infoText;
    public int curveMaxSpeed;
    public float curveAggression;
    public float curveCutoffStart;
    public float curveCutoffStop;
    public float curveStart;
    public float curveStop;
    public boolean showProgressbar;


    public BSPConfig() {
        progressNightText = "§eNight Progress [§b<countdown>§e]";
        progressLightningText = "§eThunder Progress [§b<countdown>§e]";
        infoText = "§fPlayers sleeping §7[<sleepingcolor><playerssleeping>/<players>§7]";
        curveMaxSpeed = 59;
        curveAggression = 0.5f;
        curveCutoffStart = 0f; //sets anything below to 0
        curveCutoffStop = 1f; //sets anything above to 1
        curveStart = 0f; //makes the curve start at this point
        curveStop = 1f; //makes the curve stop at this point
        showProgressbar = true;
    }

    public String getProgressNightText() {
        return progressNightText;
    }

    public void setProgressNightText(String progressNightText) {
        this.progressNightText = progressNightText;
    }

    public String getProgressLightningText() {
        return progressLightningText;
    }

    public void setProgressLightningText(String progressLightningText) {
        this.progressLightningText = progressLightningText;
    }

    public String getInfoText() {
        return infoText;
    }

    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }

    public int getCurveMaxSpeed() {
        return curveMaxSpeed;
    }

    public void setCurveMaxSpeed(int curveMaxSpeed) {
        this.curveMaxSpeed = curveMaxSpeed;
    }

    public float getCurveAggression() {
        return curveAggression;
    }

    public void setCurveAggression(float curveAggression) {
        this.curveAggression = curveAggression;
    }

    public float getCurveCutoffStart() {
        return curveCutoffStart;
    }

    public void setCurveCutoffStart(float curveCutoffStart) {
        this.curveCutoffStart = curveCutoffStart;
    }

    public float getCurveCutoffStop() {
        return curveCutoffStop;
    }

    public void setCurveCutoffStop(float curveCutoffStop) {
        this.curveCutoffStop = curveCutoffStop;
    }

    public float getCurveStart() {
        return curveStart;
    }

    public void setCurveStart(float curveStart) {
        this.curveStart = curveStart;
    }

    public float getCurveStop() {
        return curveStop;
    }

    public void setCurveStop(float curveStop) {
        this.curveStop = curveStop;
    }

    public boolean isShowProgressbar() {
        return showProgressbar;
    }

    public void setShowProgressbar(boolean showProgressbar) {
        this.showProgressbar = showProgressbar;
    }


    //--------------------------------.-------------  (1 / (stop - start)) * (playerDecimal - start)


}
