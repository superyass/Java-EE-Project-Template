package ma.superyass.restplayground.security.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toSet;
import javax.ejb.Stateless;
import javax.inject.Inject;
import ma.superyass.restplayground.security.dto.UserDTO;
import ma.superyass.restplayground.security.entities.Authority;
import ma.superyass.restplayground.security.entities.User;
import ma.superyass.restplayground.security.facades.AuthorityFacade;
import ma.superyass.restplayground.security.facades.UserFacade;
import ma.superyass.restplayground.security.utils.AuthenticationException;
import ma.superyass.restplayground.security.utils.AuthoritiesConstants;
import ma.superyass.restplayground.security.utils.PasswordEncoder;
import ma.superyass.restplayground.security.utils.SecurityUtils;
import ma.superyass.restplayground.utils.RandomUtil;

/**
 *
 * @author superyass
 */
@Stateless
public class AuthenticationService {
    
    @Inject
    private SecurityUtils securityUtils;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private UserFacade userFacade;

    @Inject
    private AuthorityFacade authorityFacade;

    public Optional<User> activateRegistration(String key) {
        Logger.getLogger(AuthenticationService.class.getName()).log(java.util.logging.Level.INFO, "Activating user for activation key {}", key);
        return userFacade.findOneByActivationKey(key)
                .map(user -> {
                    // activate given user for the registration key.
                    user.setActivated(true);
                    user.setActivationKey(null);
                    userFacade.edit(user);
                    Logger.getLogger(AuthenticationService.class.getName()).log(Level.INFO, "Activated user: {}", user);
                    return user;
                });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        Logger.getLogger(AuthenticationService.class.getName()).log(java.util.logging.Level.INFO, "Reset user password for reset key {}", key);
        return userFacade.findOneByResetKey(key)
                .filter(user -> {
                    ZonedDateTime oneDayAgo = ZonedDateTime.now().minusHours(24);
                    ZonedDateTime resetDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(user.getResetDate().getTime()), ZoneId.systemDefault());
                    return resetDate.isAfter(oneDayAgo);
                })
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    user.setResetKey(null);
                    user.setResetDate(null);
                    userFacade.edit(user);
                    return user;
                });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userFacade.findOneByEmail(mail)
                .filter(User::getActivated)
                .map( user -> {
                    user.setResetKey(RandomUtil.generateResetKey());
                    user.setResetDate(new Date());
                    userFacade.edit(user);
                    return user;
                });
    }

    public User createUser(String login, String password, String firstName, String lastName, String email,
            String langKey) {

        User newUser = new User();
        Authority authority = authorityFacade.find(AuthoritiesConstants.USER);
        Set<Authority> authorities = new HashSet<>();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(login);
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setLangKey(langKey);
        // new user is not active
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        authorities.add(authority);
        newUser.setAuthorities(authorities);
        String currentLogin = securityUtils.getCurrentUserLogin();
        newUser.setCreatedBy(currentLogin != null ? currentLogin : AuthoritiesConstants.ANONYMOUS);
        userFacade.create(newUser);
        Logger.getLogger(AuthenticationService.class.getName()).log(Level.INFO, "Created Information for User: {}", newUser);
        return newUser;
    }

    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        if (userDTO.getLangKey() == null) {
            user.setLangKey("en"); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        if (userDTO.getAuthorities() != null) {
            user.setAuthorities(userDTO.getAuthorities().stream().map(authorityFacade::find).collect(toSet()));
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(new Date());
        user.setActivated(true);
        userFacade.create(user);
        Logger.getLogger(AuthenticationService.class.getName()).log(Level.INFO, "Created Information for User: {}", user);
        return user;
    }

    public void updateUser(String firstName, String lastName, String email, String langKey) {
        userFacade.findOneByLogin(securityUtils.getCurrentUserLogin()).ifPresent(u -> {
            u.setFirstName(firstName);
            u.setLastName(lastName);
            u.setEmail(email);
            u.setLangKey(langKey);
            userFacade.edit(u);
            Logger.getLogger(AuthenticationService.class.getName()).log(Level.INFO, "Changed Information for User: {}", u);
        });
    }

    public void deleteUserInformation(String login) {
        userFacade.findOneByLogin(login).ifPresent(u -> {
            userFacade.remove(u);
            Logger.getLogger(AuthenticationService.class.getName()).log(Level.INFO, "Deleted User: {}", u);
        });
    }

    public void changePassword(String password) {
        userFacade.findOneByLogin(securityUtils.getCurrentUserLogin()).ifPresent(u -> {
            String encryptedPassword = passwordEncoder.encode(password);
            u.setPassword(encryptedPassword);
            userFacade.edit(u);
            Logger.getLogger(AuthenticationService.class.getName()).log(Level.INFO, "Changed password for User: {}", u);
        });
    }

    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userFacade.findOneWithAuthoritiesByLogin(login);
    }

    public User getUserWithAuthorities(Long id) {
        return userFacade.findOneWithAuthoritiesById(id).orElse(null);
    }

    public User getUserWithAuthorities() {
        if (securityUtils.getCurrentUserLogin() == null) {
            return null;
        }
        return userFacade.findOneWithAuthoritiesByLogin(securityUtils.getCurrentUserLogin()).orElse(null);
    }

    public User authenticate(String login, String password) throws AuthenticationException {
        Optional<User> userOptional = userFacade.findOneWithAuthoritiesByLogin(login);
        if (userOptional.isPresent() && userOptional.get().getActivated() && userOptional.get().getPassword().equals(passwordEncoder.encode(password))) {
            return userOptional.get();
        } else {
            throw new AuthenticationException();
        }
    }
    
}
