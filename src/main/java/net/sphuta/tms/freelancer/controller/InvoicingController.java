package net.sphuta.tms.freelancer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * MVC Controller for handling invoicing form requests.
 *
 * <p>
 * This controller serves the Thymeleaf (or any template engine) view
 * that allows users to fill in invoicing-related details.
 * </p>
 *
 * <p><b>Note:</b> This is different from {@link SettingsInvoicingController},
 * which is a REST API controller. This one is specifically for rendering
 * the web page (HTML form).</p>
 */
@Controller
public class InvoicingController {

    /**
     * Handles HTTP GET request for the invoicing form page.
     *
     * @return The logical name of the view template (invoicing-form.html)
     *         located under <code>resources/templates/</code>.
     */
    @GetMapping("/invoicing-form")
    public String invoicingForm() {
        // Returning the template name. Spring will resolve this to:
        // src/main/resources/templates/invoicing-form.html
        return "invoicing-form";
    }
}
