/*
 * The MIT License
 *
 * Copyright (c) 2010, Manufacture Française des Pneumatiques Michelin, Thomas Maurel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.michelin.cio.hudson.plugins.rolestrategy;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
//import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.oauth.*;
import com.synopsys.arc.jenkins.plugins.rolestrategy.Macro;
import com.synopsys.arc.jenkins.plugins.rolestrategy.RoleMacroExtension;
import com.synopsys.arc.jenkins.plugins.rolestrategy.RoleType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.User;
import hudson.security.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.acls.sid.Sid;
import org.acegisecurity.userdetails.UserDetails;
import org.jenkinsci.plugins.rolestrategy.Settings;
import org.jenkinsci.plugins.rolestrategy.permissions.DangerousPermissionHelper;
import org.json.JSONException;
import org.springframework.dao.DataAccessException;

/**
 * Class holding a map for each kind of {@link AccessControlled} object, associating
 * each {@link Role} with the concerned {@link User}s/groups.
 * @author Thomas Maurel
 */
public class RoleMap {

  /** Map associating each {@link Role} with the concerned {@link User}s/groups. */
  private final SortedMap <Role,Set<String>> grantedRoles;

  private static final Logger LOGGER = Logger.getLogger(RoleMap.class.getName());
  
  private final Cache<String, UserDetails> cache = CacheBuilder.newBuilder()
          .softValues()
          .maximumSize(Settings.USER_DETAILS_CACHE_MAX_SIZE)
          .expireAfterWrite(Settings.USER_DETAILS_CACHE_EXPIRATION_TIME_SEC, TimeUnit.SECONDS)
          .build();


  RoleMap() {
    this.grantedRoles = new TreeMap<Role, Set<String>>();
  }

  RoleMap(SortedMap<Role,Set<String>> grantedRoles) {
    this.grantedRoles = grantedRoles;
  }

  /**
   * Check if the given sid has the provided {@link Permission}.
   * @return True if the sid's granted permission
   */
  private boolean hasPermission(final String sid, Permission p, RoleType roleType, AccessControlled controlledItem) throws IOException, JSONException, ExecutionException {
      // built-in user
//      if (sid.equals(SidACL.ANONYMOUS_USERNAME) || sid.equals(SidACL.SYSTEM_USERNAME) || sid.equals("role_everyone"))
//          return checkPermission(sid, p, roleType, controlledItem);

      // generate set of parent of sid and sid itself
      Authentication auth = Jenkins.getAuthentication();

      if (auth instanceof AzureAuthenticationToken) {
          System.out.println("current auth is " + ((AzureAuthenticationToken) auth).getAzureIdTokenUser().getUniqueName());
          System.out.println("current aad token is " + ((AzureAuthenticationToken) auth).getAzureAdToken());
          System.out.println("current  rm token is " + ((AzureAuthenticationToken) auth).getAzureRmToken());
      }
      Jenkins jenkins = Jenkins.getInstance();
      if (jenkins == null) {
          throw new RuntimeException("Jenkins is not started yet.");
      }
      SecurityRealm realm = jenkins.getSecurityRealm();

      if (auth instanceof AzureAuthenticationToken && realm instanceof AzureSecurityRealm) {
          String accessToken = ((AzureAuthenticationToken) auth).getAzureAdToken().getToken();
          if (accessToken == null) return false;
//          AzureResponse response = AzureAdApi.getGroupsByUserId(accessToken);
          String upn = ((AzureAuthenticationToken) auth).getAzureIdTokenUser().getUniqueName();
          Set<String> set = AzureCachePool.getBelongingGroupsByUPN(upn);

          // plus user himself/herself
          set.add(((AzureAuthenticationToken) auth).getAzureIdTokenUser().getUniqueName());

          // check one by one
          for (String ele : set) {
              if (ele == null || ele.equals("")) continue;
              boolean hasPermission = checkPermission(ele, p, roleType, controlledItem);
              if (hasPermission) return true;
          }
          return false;
      } else {
          return checkPermission(sid, p, roleType, controlledItem);
      }



  }

    private Set<String> getGroupsUniqueId(Set<String> groups) {
      Set<String> updated = new HashSet<>();
      for(String sid: groups) {
          // built-in user types
          if (sid.equals(SidACL.ANONYMOUS_USERNAME) || sid.equals(SidACL.SYSTEM_USERNAME) || sid.equals("role_everyone")) {
              updated.add(sid);
          // user upn or group display name + oid
          } else {
              int left = sid.lastIndexOf('(') + 1;
              int right = sid.lastIndexOf(')');
              // on GUID, treat as upn
              if (left == -1 || right == -1 || right - left <= 1) {
                  updated.add(sid);
              // extract GUID
              } else {
                  String uuid = sid.substring(left, right);
                  if (!Utils.UUIDUtil.isValidUuid(uuid)) continue;
                  updated.add(uuid);
                  // TODO: what if the uuid is wrong, need to throw exception?
              }
          }
      }
      return updated;
    }

    private boolean checkPermission(final String sid, Permission p, RoleType roleType, AccessControlled controlledItem) {
        System.out.println("Role Map has Permission: " + " Sid = " + sid + " Permission = " + p + " RoleType = " + roleType + " ControlledItem = " + controlledItem);


        if (DangerousPermissionHelper.isDangerous(p)) {
      /* if this is a dangerous permission, fall back to Administer unless we're in compat mode */
            System.out.println("dangerous permissions = " + p);
            p = Jenkins.ADMINISTER;
        }




        for(Role role : getRolesHavingPermission(p)) {

            Set<String> groups = this.grantedRoles.get(role);
            Set<String> uniqueIdSet = getGroupsUniqueId(groups);
            if(uniqueIdSet.contains(sid)) {
                // Handle roles macro
                if (/*Macro.isMacro(role)*/false) {
                    Macro macro = RoleMacroExtension.getMacro(role.getName());
                    if (macro != null) {
                        RoleMacroExtension macroExtension = RoleMacroExtension.getMacroExtension(macro.getName());
                        if (macroExtension.IsApplicable(roleType) && macroExtension.hasPermission(sid, p, roleType, controlledItem, macro)) {
                            return true;
                        }
                    }
                } // Default handling
                else {
                    return true;
                }
            } else if (Settings.TREAT_USER_AUTHORITIES_AS_ROLES) {
                try {
                    UserDetails userDetails = cache.getIfPresent(sid);
                    if (userDetails == null) {
                        userDetails = Jenkins.getActiveInstance().getSecurityRealm().loadUserByUsername(sid);
                        cache.put(sid, userDetails);
                    }
                    for (GrantedAuthority grantedAuthority : userDetails.getAuthorities()) {
                        if (grantedAuthority.getAuthority().equals(role.getName())) {
                            return true;
                        }
                    }
                } catch (BadCredentialsException e) {
                    LOGGER.log(Level.FINE, "Bad credentials", e);
                } catch (DataAccessException e) {
                    LOGGER.log(Level.FINE, "failed to access the data", e);
                } catch (RuntimeException ex) {
                    // There maybe issues in the logic, which lead to IllegalStateException in Acegi Security (JENKINS-35652)
                    // So we want to ensure this method does not fail horribly in such case
                    LOGGER.log(Level.WARNING, "Unhandled exception during user authorities processing", ex);
                }
            }

            // TODO: Handle users macro
        }
        return false;
    }


  /**
   * Check if the {@link RoleMap} contains the given {@link Role}.
   * 
   * @param role Role to be checked
   * @return {@code true} if the {@link RoleMap} contains the given role
   */
  public boolean hasRole(@Nonnull Role role) {
    return this.grantedRoles.containsKey(role);
  }

  /**
   * Get the ACL for the current {@link RoleMap}.
   * @return ACL for the current {@link RoleMap}
   */
  public SidACL getACL(RoleType roleType, AccessControlled controlledItem) {
    return new AclImpl(roleType, controlledItem);
  }

  /**
   * Add the given role to this {@link RoleMap}.
   * @param role The {@link Role} to add
   */
  public void addRole(Role role) {
    if (this.getRole(role.getName()) == null) {
      this.grantedRoles.put(role, new HashSet<String>());
    }
  }

  /**
   * Assign the sid to the given {@link Role}.
   * @param role The {@link Role} to assign the sid to
   * @param sid The sid to assign
   */
  public void assignRole(Role role, String sid) {
    if (this.hasRole(role)) {
      this.grantedRoles.get(role).add(sid);
    }
  }

  /**
   * unAssign the sid to the given {@link Role}.
   * @param role The {@link Role} to unassign the sid to
   * @param sid The sid to assign
   * @since 2.6.0
   */
  public void unAssignRole(Role role, String sid) {
    if (this.hasRole(role)) {
      if (this.grantedRoles.get(role).contains(sid)) {
        this.grantedRoles.get(role).remove(sid);
      }
    }
  }

  /**
   * Clear all the sids associated to the given {@link Role}.
   * @param role The {@link Role} for which you want to clear the sids
   */
  public void clearSidsForRole(Role role) {
    if (this.hasRole(role)) {
      this.grantedRoles.get(role).clear();
    }
  }
  
  /**
   * Clear all the roles associated to the given sid
   * @param sid The sid for thwich you want to clear the {@link Role}s
   */
  public void deleteSids(String sid){
     for (Map.Entry<Role, Set<String>> entry: grantedRoles.entrySet()) {
         Role role = entry.getKey();
         Set<String> sids = entry.getValue();
         if (sids.contains(sid)) {
             sids.remove(sid);
         }
     }
  }

  /**
   * Clear specific role associated to the given sid
   * @param sid The sid for thwich you want to clear the {@link Role}s
   * @param rolename The role for thwich you want to clear the {@link Role}s
   * @since 2.6.0
   */
  public void deleteRoleSid(String sid, String rolename){
     for (Map.Entry<Role, Set<String>> entry: grantedRoles.entrySet()) {
         Role role = entry.getKey();
         if (role.getName().equals(rolename)) {
            unAssignRole(role, sid);
            break;
         }
     }
  }

  /**
   * Clear all the sids for each {@link Role} of the {@link RoleMap}.
   */
  public void clearSids() {
    for (Map.Entry<Role, Set<String>> entry : this.grantedRoles.entrySet()) {
      Role role = entry.getKey();
      this.clearSidsForRole(role);
    }
  }

  /**
   * Get the {@link Role} object named after the given param.
   * @param name The name of the {@link Role}
   * @return The {@link Role} named after the given param.
   *         {@code null} if the role is missing.
   */
  @CheckForNull
  public Role getRole(String name) {
    for (Role role : this.getRoles()) {
      if (role.getName().equals(name)) {
        return role;
      }
    }
    return null;
  }
  
  /**
   * Removes a {@link Role}
   * @param role The {@link Role} which shall be removed
   */
  public void removeRole(Role role){
      this.grantedRoles.remove(role);
  }
  

  /**
   * Get an unmodifiable sorted map containing {@link Role}s and their assigned sids.
   * @return An unmodifiable sorted map containing the {@link Role}s and their associated sids
   */
  public SortedMap<Role, Set<String>> getGrantedRoles() {
    return Collections.unmodifiableSortedMap(this.grantedRoles);
  }

  /**
   * Get an unmodifiable set containing all the {@link Role}s of this {@link RoleMap}.
   * @return An unmodifiable set containing the {@link Role}s
   */
  public Set<Role> getRoles() {
    return Collections.unmodifiableSet(this.grantedRoles.keySet());
  }

  /**
   * Get all the sids referenced in this {@link RoleMap}, minus the {@code Anonymous} sid.
   * @return A sorted set containing all the sids, minus the {@code Anonymous} sid
   */
  public SortedSet<String> getSids() {
    return this.getSids(false);
  }

  /**
   * Get all the sids referenced in this {@link RoleMap}.
   * @param includeAnonymous True if you want the {@code Anonymous} sid to be included in the set
   * @return A sorted set containing all the sids
   */
  public SortedSet<String> getSids(Boolean includeAnonymous) {
    TreeSet<String> sids = new TreeSet<>();
    for (Map.Entry<Role, Set<String>> entry : this.grantedRoles.entrySet()) {
      sids.addAll(entry.getValue());
    }
    // Remove the anonymous sid if asked to
    if (!includeAnonymous) {
      sids.remove("anonymous");
    }
    return Collections.unmodifiableSortedSet(sids);
  }

  /**
   * Get all the sids assigned to the {@link Role} named after the {@code roleName} param.
   * @param roleName The name of the role
   * @return A sorted set containing all the sids.
   *         {@code null} if the role is missing.
   */
  @CheckForNull
  public Set<String> getSidsForRole(String roleName) {
    Role role = this.getRole(roleName);
    if (role != null) {
      return Collections.unmodifiableSet(this.grantedRoles.get(role));
    }
    return null;
  }

  /**
   * Create a sub-map of the current {@link RoleMap} containing only the
   * {@link Role}s matching the given pattern.
   * @param namePattern The pattern to match
   * @return A {@link RoleMap} containing only {@link Role}s matching the given name
   */
  public RoleMap newMatchingRoleMap(String namePattern) {
    Set<Role> roles = getMatchingRoles(namePattern);
    SortedMap<Role, Set<String>> roleMap = new TreeMap<>();
    for (Role role : roles) {
      roleMap.put(role, this.grantedRoles.get(role));
    }
    return new RoleMap(roleMap);
  }

  /**
   * Get all the roles holding the given permission.
   * @param permission The permission you want to check
   * @return A Set of Roles holding the given permission
   */
  private Set<Role> getRolesHavingPermission(final Permission permission) {
    final Set<Role> roles = new HashSet<>();
    final Set<Permission> permissions = new HashSet<>();
    Permission p = permission;

    // Get the implying permissions
    for (; p!=null; p=p.impliedBy) {
      permissions.add(p);
    }
    // Walk through the roles, and only add the roles having the given permission,
    // or a permission implying the given permission
    new RoleWalker() {
      public void perform(Role current) {
        if (current.hasAnyPermission(permissions)) {
          roles.add(current);
        }
      }
    };

    return roles;
  }

  /**
   * Get all the roles whose pattern match the given pattern.
   * @param namePattern The string to match
   * @return A Set of Roles matching the given name
   */
  private Set<Role> getMatchingRoles(final String namePattern) {
    final Set<Role> roles = new HashSet<>();

    // Walk through the roles and only add the Roles whose pattern matches the given string
    new RoleWalker() {
      public void perform(Role current) {
        Matcher m = current.getPattern().matcher(namePattern);
        if (m.matches()) {
          roles.add(current);
        }
      }
    };

    return roles;
  }

  /**
   * The Acl class that will delegate the permission check to the {@link RoleMap} object.
   */
  private final class AclImpl extends SidACL {

    AccessControlled item;
    RoleType roleType;

    public AclImpl(RoleType roleType, AccessControlled item) {
        this.item = item;
        this.roleType = roleType;
    }
      
    /**
     * Checks if the sid has the given permission.
     * <p>Actually only delegate the check to the {@link RoleMap} instance.</p>
     * @param p The sid to check
     * @param permission The permission to check
     * @return True if the sid has the given permission
     */
    @SuppressFBWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "As declared in Jenkins API")
    @Override
    @CheckForNull
    protected Boolean hasPermission(Sid p, Permission permission) {
        try {
//            System.out.println("Sid = " + p);
            if (RoleMap.this.hasPermission(toString(p), permission, roleType, item)) {
              return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
  }

  /**
   * A class to walk through all the {@link RoleMap}'s roles and perform an
   * action on each one.
   */
  private abstract class RoleWalker {

    RoleWalker() {
      walk();
    }

    /**
     * Walk through the roles.
     */
    public void walk() {
      Set<Role> roles = RoleMap.this.getRoles();
      Iterator<Role> iter = roles.iterator();
      while (iter.hasNext()) {
        Role current = iter.next();
        perform(current);
      }
    }

    /**
     * The method to implement which will be called on each {@link Role}.
     */
    abstract public void perform(Role current);
  }
}
