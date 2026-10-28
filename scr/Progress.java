import java.util.prefs.Preferences;
public class Progress {
    private static final Preferences PREF=Preferences.userNodeForPackage(Progress.class);
    public static int unlocked(){return Math.max(1,Math.min(10,PREF.getInt("unlocked",1)));}
    public static void complete(int level){if(level>=unlocked())PREF.putInt("unlocked",Math.min(10,level+1));}
}