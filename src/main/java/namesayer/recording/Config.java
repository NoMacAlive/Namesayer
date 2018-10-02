package namesayer.recording;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Defines constants which are mainly folder names
 */
public class Config {
    public static final Path CREATIONS_FOLDER = Paths.get("recordings");
    public static final Path TEMP_RECORDINGS = Paths.get("temp");
    public static final Path SAVED_RECORDINGS = Paths.get("saved");
    public static final String WAV_EXTENSION = ".wav";
    public static final Path RATINGS = Paths.get("ratings.txt");
    public static final Path DATA_BASE  = Paths.get("database");
}
