package api.event;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.assertj.core.util.Files;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import api.exceptionHandling.CustomException;
import api.model.User;
import api.model.VerificationToken;
import api.repository.VerificationTokenRepository;

@Component
public class RegistrationCompleteListener implements ApplicationListener<OnRegistrationCompleteEvent> {
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private VerificationTokenRepository tokenRepo;
	
	@Autowired
	private MessageSource messages;
	
	@Autowired
	private JavaMailSender mailSender;

	@Value("${FRONT_END_ROOT_URL:http://localhost:8080}") // TODO: add this var to heroku conf
	private String frontEndRootUrl;

	@Value("classpath:Verification.customEmailTemplate")
	private Resource verifTmpltRes;
	
	/* -------------------------------------------------------------------------- */
	/*                                   Methods                                  */
	/* -------------------------------------------------------------------------- */

	@Override
	public void onApplicationEvent(OnRegistrationCompleteEvent event) {
		this.confirmRegistration(event);
	}

	private void confirmRegistration(OnRegistrationCompleteEvent event) {
		User user = event.user;
		VerificationToken tokenEntity = new VerificationToken(user);
		tokenRepo.save(tokenEntity);

		String verificationLink = frontEndRootUrl + "/confirm-registration/" + tokenEntity.token;

		SimpleMailMessage email = new SimpleMailMessage();
		email.setTo(user.getEmail());
		email.setSubject("Registration Confirmation");
		email.setText( emailBody(user, frontEndRootUrl, verificationLink) );
		mailSender.send(email);
	}

	private String emailBody(User user, String frontEndRootUrl, String verificationLink) {
		String verifTmplt;
		try {
			verifTmplt = Files.contentOf(this.verifTmpltRes.getFile(), Charset.defaultCharset());	
		} catch ( IOException e) {
			throw new CustomException("User was registered but the activation email could not be sent(email template)."); //TODO:Label
			//TODO error number uniquely referenced in the code.
		}

		verifTmplt = verifTmplt.replace("${username}", user.getUsername());
		verifTmplt = verifTmplt.replace("${frontEndRootUrl}", frontEndRootUrl);
		verifTmplt = verifTmplt.replace("frontEndRootUrl", frontEndRootUrl);
		verifTmplt = verifTmplt.replace("${verificationLink}", verificationLink);
		verifTmplt = verifTmplt.replace("${email}", user.getEmail());

		return verifTmplt;
	}
}