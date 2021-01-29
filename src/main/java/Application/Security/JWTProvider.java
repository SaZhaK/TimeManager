package Application.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.TextCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class JWTProvider {
    private static final String JWT_SECRET = "J3Q011DbM6ogZrlZMzFB38Mh2jXTeBXnBRc76mi83dH0MjxzcORnmlo4F0RjVASS";
    private static final String KEY = TextCodec.BASE64.encode(JWT_SECRET);

    public static String generateToken(long id, String login, String password) {
        if (login == null || password == null || id < 0) {
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
            log.error("Token expired");
        } catch (UnsupportedJwtException unsEx) {
            log.error("Unsupported jwt");
        } catch (MalformedJwtException mjEx) {
            log.error("Malformed jwt");
        } catch (SignatureException sEx) {
            log.error("Invalid signature");
        } catch (Exception e) {
            log.error("invalid token");
        }
        return false;
    }

    public static long getId(String token) {
        if (token == null) {
            throw new IllegalArgumentException("can not get id from null token");
        }

        Claims claims = Jwts.parser()
                .setSigningKey(KEY)
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getId());
    }

    public static String getLogin(String token) {
        if (token == null) {
            throw new IllegalArgumentException("can not get login from null token");
        }

        Claims claims = Jwts.parser()
                .setSigningKey(KEY)
                .parseClaimsJws(token)
                .getBody();

        return claims.get("login").toString();
    }

    public static String getPassword(String token) {
        if (token == null) {
            throw new IllegalArgumentException("can not get password from null token");
        }

        Claims claims = Jwts.parser()
                .setSigningKey(KEY)
                .parseClaimsJws(token)
                .getBody();

        return claims.get("password").toString();
    }
}
