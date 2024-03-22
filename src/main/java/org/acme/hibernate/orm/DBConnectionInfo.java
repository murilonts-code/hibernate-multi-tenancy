package org.acme.hibernate.orm;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Table(name = "known_dbs")
@Entity
@NamedQuery(name = "DBConnectionInfo.findAll", query = "SELECT d FROM DBConnectionInfo d")
@NamedQuery(name = "DBConnectionInfo.findby", query = "SELECT d FROM DBConnectionInfo d where d.username = :username")
public class DBConnectionInfo {

    @Id
    private Long id;
    private String host;
    private int port;
    private String username;
    private String password;
    private String db;

    public DBConnectionInfo(String host, int port, String user, String password, String db) {
        this.host = host;
        this.port = port;
        this.username = user;
        this.password = password;
        this.db = db;
    }

    public DBConnectionInfo() {
    }

    @Override
    public String toString() {
        return "DBConnectionInfo{" +
               "id=" + id +
               ", host='" + host + '\'' +
               ", port=" + port +
               ", username='" + username + '\'' +
               ", password='" + password + '\'' +
               ", db='" + db + '\'' +
               '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String user) {
        this.username = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }
}
