package io.config;

public class Dataset {

    private String name;
    private String type;
    private String path;

    public Dataset(String name, String path){
        this.name = name;
        this.path = path;

    }

    public Dataset(String name, String type, String path) {
        this.name = name;
        this.type = type;
        this.path = path;
    }

    /* ===== Tools ===== */

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Dataset{");
        sb.append("name='").append(name).append('\'');
        if(type!=null) sb.append(", type='").append(type).append('\'');
        sb.append(", path='").append(path).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /* ===== Getters & Setters =====*/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
