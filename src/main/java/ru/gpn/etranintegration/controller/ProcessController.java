package ru.gpn.etranintegration.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.gpn.etranintegration.service.process.Process;

@RestController("/process")
class ProcessController {

    ProcessController(@Qualifier("invoiceProcess") Process invoiceProcess) {
        this.invoiceProcess = invoiceProcess;
    }

    private final Process invoiceProcess;

    @GetMapping("/all-invoice")
    void loadAllProcess() {
        invoiceProcess.processing();
    }

    @GetMapping("/invoice-by-id")
    void loadInvoiceById(@RequestParam("invoiceId") String invoiceId,
                         @RequestParam("invoiceNum") String invoiceNum) {
        invoiceProcess.processingByInvoiceId(invoiceId, invoiceNum);
    }

}
