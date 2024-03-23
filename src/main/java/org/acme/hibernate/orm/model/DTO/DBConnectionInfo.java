package org.acme.hibernate.orm.model.DTO;


public record DBConnectionInfo(Long id, String host, int port, String username, String password, String db) {

}
