package nsu.fit.javaperf;

public class CountingThread extends Thread {
    CountingThread() { }

    @Override
    public void run() {
        System.out.println("Number of loaded classes: " + Agent.counter);
    }

}
