package api.model;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
public class VerificationToken extends EntityRoot {

    private static final int EXPIRATION_IN_MIN = 60 * 24;

    public String token;
    
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    public User user;

    @Temporal(value=TemporalType.TIMESTAMP)
    @Column(name="expiration_date")
    public Date expirationDate;
    
    private Date calculateExpirationDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return cal.getTime();
    }

    
    /* -------------------------------------------------------------------------- */
    /*                                 Constructor                                */
    /* -------------------------------------------------------------------------- */
    public VerificationToken(User user) {
        this.user = user;
        this.expirationDate = calculateExpirationDate(EXPIRATION_IN_MIN);
        this.token = UUID.randomUUID().toString(); // 128-bit value
    }
    
}
