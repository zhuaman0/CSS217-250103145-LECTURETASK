// Task 2: The View Counter (Logging Proxy)
interface Video {
    void play();
}

class RealVideo implements Video {
    private final String fileName;

    public RealVideo(String fileName) {
        this.fileName = fileName;
        System.out.println("--> [DOWNLOADING] Connecting to server & buffering 4K video: " + fileName);
    }

    @Override
    public void play() {
        System.out.println("Playing video: " + fileName);
    }
}

class ProxyVideo implements Video {
    private final String fileName;
    private RealVideo realVideo;
    private int viewCount = 0;

    public ProxyVideo(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void play() {
        viewCount++;
        System.out.println("--> [AUDIT] Video has been played " + viewCount + " time(s).");
        if (realVideo == null) {
            realVideo = new RealVideo(fileName); // lazy loading
        }
        realVideo.play();
    }
}

public class Task2 {
    public static void main(String[] args) {
        Video video = new ProxyVideo("Cat_Memes.mp4");

        video.play();
        System.out.println();
        video.play();
        System.out.println();
        video.play();
    }
}