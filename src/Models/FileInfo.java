package Models;

public class FileInfo {
    private int perm;
    private String owner;

    public FileInfo(String owner, int perm) {
        this.owner = owner;
        this.perm = perm;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getPerm() {
        return perm;
    }

    public void setPerm(int perm) {
        this.perm = perm;
    }
}
