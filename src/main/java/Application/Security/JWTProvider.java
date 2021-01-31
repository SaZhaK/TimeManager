package Application.Security;

import com.auth0.jwt.exceptions.JWTDecodeException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.TextCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a utility class for generating and processing JWT
 * All methods should be static and no instances of this class should be created
 *
 * @author sazha
 */
@Slf4j
@Service
public class JWTProvider {
    private static final String JWT_SECRET = "J3Q011DbM6ogZrlZMzFB38Mh2jXTeBXnBRc76mi83dH0MjxzcORnmlo4F0RjVASS";
    private static final String KEY = TextCodec.BASE64.encode(JWT_SECRET);

    private JWTProvider() {
    }

    /**
     * Generates new JWT basing on given credentials
     * Resulting JWT contains login, password, jti created from id, iat, nbf
     * Signed using HS256 algorithm
     *
     * @param id       - id to be used as jti
     * @param login    - user login to be placed in claims
     * @param password - user login to be placed in claims
     * @throws IllegalArgumentException if id was negative or zero or login or password was null
     */
    public static String generateToken(long id, String login, String password) {
        if (login == null || password == null || id <= 0) {
            throw new IllegalArgumentException("wrong parameters for token generation");
        }

        Date now = new Date();

        Map<String, Object> claims = new HashMap<>();
        claims.put("login", login);
        claims.put("password", password);

        return Jwts.builder()
                .setId(String.valueOf(id))
                .setIssuedAt(now)
                .setNotBefore(now)
                .setClaims(claims)
                .signWith(
                        SignatureAlgorithm.HS256,
                        KEY)
                .compact();
    }

    /**
     * Validating given token with secret key
     * This method should NOT throw anything except IllegalArgumentException
     * Logs additional information on INFO level if token is not valid
     *
     * @param token - token to be validated
     * @throws IllegalArgumentException if given token was null
     */
    public static boolean validateToken(String token) {
        if (token == null) {
            throw new IllegalArgumentException("can not validate null token");
        }

        try {
            Jwts.parser()
                    .setSigningKey(KEY)
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.info("Token expired");
        } catch (UnsupportedJwtException unsEx) {
            log.info("Unsupported jwt");
        } catch (MalformedJwtException mjEx) {
            log.info("Malformed jwt");
        } catch (SignatureException sEx) {
            log.info("Invalid signature");
        } catch (Exception e) {
            log.info("invalid token");
        }
        return false;
    }

    /**
     * Extracts id from given token
     * Logs additional information on INFO level if token can not be parsed for any reason
     *
     * @param token - token to be validated
     * @throws IllegalArgumentException if given token was null
     * @throws JWTDecodeException       if extracted id was null
     */
    public static long getId(String token) {
        if (token == null) {
            throw new IllegalArgumentException("can not get id from null token");
        }

        Claims claims = Jwts.parser()
                .setSigningKey(KEY)
                .parseClaimsJws(token)
                .getBody();

        String id;
        if ((id = claims.getId()) != null) {
            return Long.parseLong(id);
        } else {
            log.info("Unable to get id from given token");
            throw new JWTDecodeException("No id is present in given token");
        }
    }

    /**
     * Extracts login from given token
     * Logs additional information on INFO level if token can not be parsed for any reason
     *
     * @param token - token to be validated
     * @throws IllegalArgumentException if given token was null
     * @throws JWTDecodeException       if extracted login was null
     */
    public static String getLogin(String token) {
        if (token == null) {
            throw new IllegalArgumentException("can not get login from null token");
        }

        Claims claims = Jwts.parser()
                .setSigningKey(KEY)
                .parseClaimsJws(token)
                .getBody();

        Object login;
        if ((login = claims.get("login")) != null) {
            return login.toString();
        } else {
            log.error("Unable to get login from given token");
            throw new JWTDecodeException("No login is present in given token");
        }
    }

    /**
     * Extracts password from given token
     * Logs additional information on INFO level if token can not be parsed for any reason
     *
     * @param token - token to be validated
     * @throws IllegalArgumentException if given token was null
     * @throws JWTDecodeException       if extracted [assword was null
     */
    public static String getPassword(String token) {
        if (token == null) {
            throw new IllegalArgumentException("can not get password from null token");
        }

        Claims claims = Jwts.parser()
                .setSigningKey(KEY)
                .parseClaimsJws(token)
                .getBody();

        Object password;
        if ((password = claims.get("password")) != null) {
            return password.toString();
        } else {
            log.error("Unable to get password from given token");
            throw new JWTDecodeException("No password is present in given token");
        }
    }
}
