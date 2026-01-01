package pacman;

public class TestGhostScaredStatus {
    public static void main(String[] args) {
        GhostScaredStatus status = new GhostScaredStatus(0);
        System.out.println("isActive() for duration 0: " + status.isActive());
        System.out.println("startTime: " + status.getStartTime());
        System.out.println("currentTime: " + System.currentTimeMillis());
        System.out.println("elapsed: " + (System.currentTimeMillis() - status.getStartTime()));
        System.out.println("duration: " + status.getDuration());
    }
}
