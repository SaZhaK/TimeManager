package Application.Controllers;

import Application.Entities.User;
import Application.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;

@Controller
public class AdminController {

    @Autowired
    UserService userService;

    @GetMapping("admin/users")
    public String getAdminPage(Model model) {
        Collection<User> users = userService.getAllUsers();

        model.addAttribute("users", users);

        return "admin";
    }
}
