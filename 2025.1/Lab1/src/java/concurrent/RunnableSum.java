public class RunnableSum implements Runnable{
    private String path;

    public RunnableSum(String path) {
        this.path = path;
    }

    @Override
    public void run() {
        try {
            Sum.sum_p(this.path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
