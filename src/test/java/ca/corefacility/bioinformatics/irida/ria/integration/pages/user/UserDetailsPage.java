package ca.corefacility.bioinformatics.irida.ria.integration.pages.user;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * User details page for selenium testing
 * 
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 *
 */
public class UserDetailsPage {
	public static String EDIT_USER_LINK = "editUser";
	public static String USER_ID = "user-id";
	private WebDriver driver;

	public UserDetailsPage(WebDriver driver) {
		this.driver = driver;
	}

	public void getCurrentUser() {
		driver.get("http://localhost:8080/users/current");
	}

	public void getOtherUser(Long id) {
		driver.get("http://localhost:8080/users/" + id);
	}
	
	public String getUserId(){
		WebElement findElement = driver.findElement(By.id(USER_ID));
		return findElement.getText();
	}

	public boolean canGetEditLink(Long id) {
		driver.get("http://localhost:8080/users/" + id);
		try {
			driver.findElement(By.id(EDIT_USER_LINK));
			return true;
		} catch (NoSuchElementException ex) {
			return false;
		}
	}

	public List<String> getUserProjectIds() {
		List<WebElement> findElements = driver.findElements(By.className("user-project-id"));
		List<String> ids = new ArrayList<>();
		findElements.forEach(ele -> {
			ids.add(ele.getText());
		});
		return ids;
	}
	
	public void sendPasswordReset(){
		
	}
}
