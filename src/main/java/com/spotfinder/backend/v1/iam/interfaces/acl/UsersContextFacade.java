package com.spotfinder.backend.v1.iam.interfaces.acl;

/**
 * UsersContextFacade
 */
public interface UsersContextFacade {

    /**
     * Check if a user exists by id
      * @param id The user id
     * @return true if a user exists, false otherwise
     */
    boolean exitsUserById(Long id);
}
