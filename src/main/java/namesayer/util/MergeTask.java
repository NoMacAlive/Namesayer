package namesayer.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ChoiceDialog;
import namesayer.recording.Config;
import namesayer.recording.Name;
import namesayer.recording.NameStorageManager;
import namesayer.recording.Recording;

public class MergeTask extends Task<String>{
	
	List<Name> namesToMerge = new ArrayList<>();
	String newName;
	Name conName;
	
	public MergeTask(List<Name> names){
		namesToMerge = names;
		conName = new Name(names);
		NameStorageManager manager = NameStorageManager.getInstance();
		manager.addNewNametoList(conName);
	}
	
	@Override
	protected String call() throws Exception{
		String directory = System.getProperty("user.dir");
		File listToMerge  = new File(directory + "/recordings/listToMerge.txt");
		if(listToMerge.exists()) {
			listToMerge.delete();
		}
		
		
		//write all the name paths in MergeNames 
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(directory + "/MergeNames.txt"), "utf-8"));
		for(Name name: namesToMerge) {
			writer.write("file "+name.getARecording().getRecordingPath().toString());
			writer.write("\n");
		}
		writer.close();


            String cmd = "ffmpeg -y -f concat -safe 0 -i \"MergeNames.txt\" -c copy "+ conName.getDirectory()+"/saved/"+conName.getName()+Config.WAV_EXTENSION;
			System.out.println(cmd);
            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd);
            try {
                Process process = builder.start();
                process.waitFor();
                conName.addSavedRecording(new Recording(Paths.get(conName.getDirectory()+"/saved/"+conName.getName()+Config.WAV_EXTENSION)));
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

		return "Complete";
	}
	@Override
	protected void done() {
		
	}
	

}