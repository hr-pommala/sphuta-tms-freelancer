package net.sphuta.tms.freelancer.schedulers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Optional helper: prints whether invoiceScheduler bean is present in context.
 * Remove/delete after you confirm scheduling is working.
 */
@Component
public class BeanChecker implements CommandLineRunner {

    @Autowired
    private ApplicationContext ctx;

    @Override
    public void run(String... args) {
        boolean has = ctx.containsBean("invoiceScheduler");
        System.out.println(">> InvoiceScheduler bean present = " + has);
        if (has) {
            System.out.println(">> invoiceScheduler bean type = " + ctx.getBean("invoiceScheduler").getClass().getName());
        }
    }
}
