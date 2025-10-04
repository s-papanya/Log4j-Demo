package com.example.vuln.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.vuln.dto.SignupForm;
import com.example.vuln.model.UserAccount;
import com.example.vuln.service.UserService;

@Controller
public class AuthController {

    private static final Logger logger = LogManager.getLogger(AuthController.class);
    private final UserService userService;

    // ใช้ constructor injection ให้ Spring สร้างให้
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    // === [1] หน้า Login (GET) — ไม่ยุ่ง session/ไม่ส่งคุกกี้ ===
    @GetMapping({"/", "/login"})
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            Model model,
                            HttpServletRequest request) {
        // ห้ามรับ HttpSession/HttpServletResponse ที่นี่
        // ป้องกันการสร้าง JSESSIONID โดยไม่ตั้งใจ
        if (error != null) model.addAttribute("loginError", true);

        // ถ้ามี session อยู่แล้วและล็อกอินแล้ว จะเด้งเข้าหน้า dashboard เลย (ทางเลือก)
        HttpSession existing = request.getSession(false);
        if (existing != null && existing.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }

        return "login";
    }

    // === [2] ทำ Login (POST) — สร้าง session/ส่งคุกกี้เฉพาะกรณีสำเร็จ ===
    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Model model) {

        String ua = request.getHeader("User-Agent");
        String clientIp = request.getRemoteAddr();
        logger.info("User-Agent: {}", ua);
        logger.info("Client IP: {}", clientIp);
        logger.info("Login attempt for user: {}", username);

        // อย่าสร้าง session ตรงนี้ถ้ายังไม่ auth ผ่าน
        // ถ้ามี session ค้าง (เช่นจากที่อื่น) ก็ยังไม่แตะ
        return userService.findByUsername(username)
            .filter(u -> userService.verifyPassword(password, u.getPasswordHash()))
            .map(u -> {
                // ✅ auth ผ่าน ค่อย "สร้าง" session และ set attribute
                HttpSession session = request.getSession(true); // <— สร้างตอนนี้เท่านั้น
                session.setAttribute("user", u.getUsername());

                // ✅ ค่อย "ส่งคุกกี้" ณ ตอนนี้เท่านั้น
                Cookie stage = new Cookie("login_stage", "ok");
                stage.setPath("/");
                stage.setHttpOnly(true);
                stage.setSecure(false);      // เปิด HTTPS จริง ค่อยใส่ true
                stage.setMaxAge(900);
                response.addCookie(stage);
                
                logger.info("Successful login for user: {}", username);
                return "redirect:/dashboard";
            })
            .orElseGet(() -> {
                logger.warn("Failed login attempt for user: {}", username);
                // ❌ auth ไม่ผ่าน: ไม่สร้าง session ไม่ addCookie ใด ๆ เลย
                return "redirect:/login?error=1";
            });
    }


    // === [3] หน้า Dashboard (ต้องล็อกอินถึงดูได้) ===
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", user.toString());
        return "dashboard"; // -> templates/dashboard.html
    }

    // === [4] Signup (GET) ===
    @GetMapping("/signup")
    public String signupPage(Model model) {
        if (!model.containsAttribute("signupForm")) {
            model.addAttribute("signupForm", new SignupForm());
        }
        return "signup"; // -> templates/signup.html
    }

    // === [4] Signup (POST) – ของจริง ===
    @PostMapping(path = "/signup", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String signup(@Valid @ModelAttribute("signupForm") SignupForm form,
                         HttpServletRequest request,
                         RedirectAttributes redirect) {

        String ua = request.getHeader("User-Agent");
        String clientIp = request.getRemoteAddr();

        logger.info("Signup request UA={}, IP={}", ua, clientIp);
        logger.info("Signup attempt for user: {}, email: {}", form.getUsername(), form.getEmail());

        try {
            UserAccount u = userService.register(form);
            logger.info("User registration successful for: {}", u.getUsername());
            redirect.addFlashAttribute("signupSuccess", true);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            String code = e.getMessage(); // "username_taken" / "email_taken"
            redirect.addFlashAttribute("signupError", code);
            redirect.addFlashAttribute("signupForm", form);
            return "redirect:/signup";
        }
    }

    // === [5] Logout ===
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // ตัด session server-side
        return "redirect:/login";
    }

    @GetMapping("/cause-error")
    @ResponseBody
    public String causeError() {
        throw new RuntimeException("Demo exception to show Whitelabel / stacktrace");
    }

    @GetMapping("/test-jsp")
    public String testJsp(Model model) {
        model.addAttribute("msg", "This is a JSP page — evidence of Java/JSP");
        return "test"; // maps to /WEB-INF/jsp/test.jsp
    }
}
