import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;
import edu.cmu.sphinx.util.TimeFrame;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

public class Test {
    
    static ArrayList<String> curses = new ArrayList<String>();
    
    public static String doTest (String filepath, String filedirec, Curtis curtis) throws IOException
    {
        FFmpeg ffmpeg = new FFmpeg("./ffmpeg");
        FFprobe ffprobe = new FFprobe("./ffprobe");
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        
        Scanner scan = new Scanner(new File("curse.txt"));
        while (scan.hasNextLine())
            curses.add(scan.nextLine());
        
        Configuration configuration = new Configuration();

        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
        configuration.setGrammarPath("file:src");
        configuration.setGrammarName("hello");
        configuration.setUseGrammar(false);

        
        StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
        
        //copy audio file to the user's destination
        System.out.println(filepath);
        System.out.println(filedirec);
        
        new File(filedirec + "/audio.wav").delete();
        new File(filedirec + "/audio.orig.wav").delete();
        
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(filepath)
                .overrideOutputFiles(true)
                .addOutput(filedirec + "/audio.wav")
                .setAudioSampleRate(16000)
                .done();
        
        executor.createJob(builder).run();builder = new FFmpegBuilder()
                .setInput(filepath)
                .overrideOutputFiles(true)
                .addOutput(filedirec + "/audio.orig.wav")
                .setAudioSampleRate(16000)
                .done();
        
        executor.createJob(builder).run();
        
        //stream will be that new location
//        String oldFilepath = filepath;
        filepath = filedirec + "/audio.wav";
        
        InputStream stream = new FileInputStream(new File(filedirec + "/audio.orig.wav"));

        recognizer.startRecognition(stream);
        SpeechResult result;
        while ((result = recognizer.getResult()) != null) {
            System.out.format("Hypothesis: %s\n", result.getHypothesis());
            // Get individual words and their times.
            for (WordResult r : result.getWords()) 
            {
                System.out.println(r);
                //check if word is swear
                if(search(r.getWord().toString()))
                {
                    //beep out the audio
                    
                    //isolate all audio before and after word
                    TimeFrame tf = r.getTimeFrame();
                    String ts1 = prettyMillis(tf.getStart());
                    String ts2 = prettyMillis(tf.getEnd());
//                    String duration = prettyMillis(tf.length());
                    
                    builder = new FFmpegBuilder()
                            .setInput(filepath)
                            .overrideOutputFiles(true)
                            .addExtraArgs("-t", ts1)
                            .addOutput(filedirec + "/start.wav")
                            .done();
                    executor.createJob(builder).run();
                    
                    builder = new FFmpegBuilder()
                            .setInput(filepath)
                            .overrideOutputFiles(true)
                            .addExtraArgs("-ss", ts2)
                            .addOutput(filedirec + "/end.wav")
                            .done();
                    executor.createJob(builder).run();
                    
                    //replace word with beep of the same duration
                    builder = new FFmpegBuilder()
                            .addExtraArgs("-stream_loop", "-1")
                            .setInput("censor.wav")
                            .addExtraArgs("-t")
                            .addExtraArgs(prettyMillis(tf.getEnd() - tf.getStart()))
                            .overrideOutputFiles(true)
                            .addOutput(filedirec + "/beep.wav")
                            .done();
                    executor.createJob(builder).run();
                    
                    //concatenate all of those together and replace file with it
                    new File(filepath).delete();
                    builder = new FFmpegBuilder() {
                        public List<String> build() {
                          List<String> sbuild = new ArrayList<>(super.build());
                          for(int i=1; i<sbuild.size(); i++) {
                              if(sbuild.get(i).isEmpty() && sbuild.get(i-1).equals("-i")) {
                                  sbuild.remove(i);
                                  sbuild.remove(i-1);
                                  i -= 2;
                              }
                          }
                          
                          return sbuild;
                        }
                    }
                            .addInput("")
                            .addExtraArgs("-i", filedirec + "/start.wav")
                            .addExtraArgs("-i", filedirec + "/beep.wav")
                            .addExtraArgs("-i", filedirec + "/end.wav")
                            .overrideOutputFiles(true)
                            .addExtraArgs("-filter_complex", "[0:a:0][1:a:0][2:a:0]concat=n=3:v=0:a=1[outa]")
                            .addExtraArgs("-map", "[outa]")
                            .addOutput(filepath + ".tmp.wav")
                            .done();
                    System.out.println(builder.build());
                    executor.createJob(builder).run();
                    
                    new File(filepath).delete();
                    new File(filepath + ".tmp.wav").renameTo(new File(filepath));

                    //delete all temporary files
                    new File(filedirec + "/start.wav").delete();
                    new File(filedirec + "/end.wav").delete();
                    new File(filedirec + "/beep.wav").delete();
                }
            }
        }
        recognizer.stopRecognition();
        new File(filedirec + "/audio.orig.wav").delete();
        
        scan.close();
        //Runtime.getRuntime().exec("command");
        return "yes";
    }
    
    public static boolean search(String str)
    {
        for(int i = 0; i < curses.size(); i++)
        {
            if (str.contains(curses.get(i)))
                return true;
        }
        return false;
        
    }
    
    public final static String prettyMillis(long mil) {
        long h, m, s;
        long millis = mil;
        h = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(h);

        m = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(m);

        s = TimeUnit.MILLISECONDS.toSeconds(millis);
        millis -= TimeUnit.SECONDS.toMillis(s);
        return String.format("%02d:%02d:%02d.%03d", h, m, s, millis);
    }
}
