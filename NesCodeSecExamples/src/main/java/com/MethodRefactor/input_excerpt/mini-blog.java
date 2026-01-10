```<|start_of_file|>
<|editable_region_start|>
package mini.service;

import java.util.Calendar;
import java.util.Date;

import mini.dao.TokenDAOInterface;
import mini.dao.UserDAOInterface;
import mini.model.Token;
import mini.model.Users;
import mini.systemvalue.SystemValue;
import mini.util.HashGenUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Le Dang Huy
 */
@Service
public class TokenService implements TokenServiceInterface
{

    @Autowired
    private TokenDAOInterface token_DAO;

    @Autowired
    private UserDAOInterface user_DAO;

    @Override
    @Transactional
    // TODO: Extract access token generation into a helper method to reduce noise in createToken().
    public Token createToken(Users user)
    {

        String access_token = null;
        while (access_token == null) {
            try {
                access_token = generateAccessToken(user); <|user_cursor_is_here|>
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("hash error");
            }
        }
        Token token = new Token();
        token.setAccess_token(access_token);
        token.setUser(user);
        Calendar cal = Calendar.getInstance();

        token.setCreate_at(cal.getTime());

        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, SystemValue.TOKEN_EXPIRE_TIME);

        token.setExpired(cal.getTime());

        user.getUser_token().add(token);
        try {
            user_DAO.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return token;
    }

    @Override
    @Transactional
    public Token checkAccessToken(String access_token)
    {

        try {
            Token token = token_DAO.getTokenByAccessToken(access_token);
            if (token == null) {
                return null;
            }

            if (checkExpireTime(token.getExpired()) >= 0) {
                token_DAO.delete(token);
                return null;
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.HOUR_OF_DAY, SystemValue.TOKEN_EXPIRE_TIME);

                token.setExpired(cal.getTime());
                token_DAO.update(token);
                return token;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int checkExpireTime(Date expire)
    {

        Calendar cal = Calendar.getInstance();
        cal.setTime(expire);
        return Calendar.getInstance().compareTo(cal);
    }

    @Override
    @Transactional
    public boolean clearTokenTalbeByUserId(int user_id)
    {

        try {
            token_DAO.clearTokenByUserId(user_id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
<|editable_region_end|>
```