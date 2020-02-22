
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class Curtis extends JFrame
{
    
    
    String filePath="", filedirec="";
    private static JLabel filename = new JLabel("Open the file.");

    JPanel p;
    JButton open, folder;
    JProgressBar bar;
    JLabel process, finish;

    private static File file = null;
    private static File dir = null;
    public Curtis()
    {

        filename.setFont(new Font("Comic Sans MS", Font.PLAIN, 24));

        p = new JPanel();
        open = new JButton("Open File");
        open.setBorder(BorderFactory.createLineBorder(Color.black));
        open.setPreferredSize(new Dimension(120,40));

        folder = new JButton("Set Folder");
        folder.setBorder(BorderFactory.createLineBorder(Color.black));
        folder.setPreferredSize(new Dimension(120,40));

        bar = new JProgressBar(0);
        bar.setValue(0);
        bar.setStringPainted(true);

        process = new JLabel("\nProcessing file... please wait.");
        process.setHorizontalAlignment(SwingConstants.CENTER);


        filename.setHorizontalAlignment(SwingConstants.CENTER);

        open.addActionListener(new OpenL());
        p.add(open);
        p.add(folder);
        Container cp = getContentPane();
        cp.add(p, BorderLayout.SOUTH);

        folder.addActionListener(new OpenF());
        p = new JPanel();
        p.setLayout(new GridLayout(3, 3));
        p.add(filename);
        cp.add(p, BorderLayout.NORTH);

    }

    public void addBar()
    {
        p.add(process);
        p.add(bar);
    }

    public void updateBar(int percent)
    {
        bar.setValue(percent);
    }

    public void errorFinish()
    {
        process.setText("Process ended unexpectedly. Please try again.");
    }

    public void successfulFinish()
    {
        process.setText("Process completed!");

    }

    class OpenL implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            if(e.getSource() == open)
            {
                JFileChooser c = new JFileChooser();
                c.setAcceptAllFileFilterUsed(false);
                c.addChoosableFileFilter(new FileNameExtensionFilter("Video (.mp4, .wlv, .flv, .mov, .avi)", "mp4", "wlv", "flv", "mov", "avi"));
                c.addChoosableFileFilter(new FileNameExtensionFilter("Audio (.mp3, .wav, .flac, .ogg, .m4a)", "mp3", "wav", "flac", "ogg", "m4a"));


                // Demonstrate "Open" dialog:
                int rVal = c.showOpenDialog(Curtis.this);
                if (rVal == JFileChooser.APPROVE_OPTION) 
                {

                    file = c.getSelectedFile();
                    if(file != null)
                    {
                        filePath = file.getAbsolutePath();
                        filename.setText(filePath);
                        System.out.println(filePath);
                        //String extension = getExtension(filePath);
                        //other class
                        try {
                            isReady();
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        

                    }
                }
                if (rVal == JFileChooser.CANCEL_OPTION) 
                {
                    filename.setText("You pressed cancel");
                }
            }
        }
    }

    class OpenF implements ActionListener
    {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == folder)
            {
                JFileChooser c = new JFileChooser();
                c.setAcceptAllFileFilterUsed(false);
                c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int rVal = c.showOpenDialog(Curtis.this);
                if (rVal == JFileChooser.APPROVE_OPTION) 
                {
                    dir = c.getSelectedFile();
                    if(dir != null)
                    {
                        filedirec = dir.toString();
                        System.out.println(dir);
                        //String extension = getExtension(filePath);
                        //other class
                        try {
                            isReady();
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    
    public String isReady() throws IOException
    {
        if(filePath != "" && filedirec != "")
        {
            addBar();
            String result =  Test.doTest(filePath, filedirec, this);
            if(result == "yes")
            {
                successfulFinish();
            }
            else
            {
                errorFinish();
            }
            return result;
        }
        else
            return "";
    }

    public static void main(String[] args)
    {
        JFrame frame = new Curtis();
        JFrame.setDefaultLookAndFeelDecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}


