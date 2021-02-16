package Application.Controllers;

import Application.Entities.User;
import Application.Exceptions.UserNotFoundException;
import Application.Services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collection;

@Slf4j
@Controller
public class AdminController {

    @Autowired
    UserService userService;

    @GetMapping("/users")
    public String getAdminPage(Model model) {
        Collection<User> users = userService.getAllUsers();
        model.addAttribute("users", users);

        return "admin";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") long id, Model model) {
        try {
            userService.deleteUser(id);
        } catch (UserNotFoundException e) {
            log.info("User with id " + id + " not found");
        }

        Collection<User> users = userService.getAllUsers();
        model.addAttribute("users", users);

        return "redirect:/users";
    }
}
