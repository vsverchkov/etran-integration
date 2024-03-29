package ru.gpn.etranintegration.service.ibpd;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gpn.etranintegration.model.etran.message.invoice.InvoiceResponse;
import ru.gpn.etranintegration.model.ibpd.LastOperDateResponse;
import ru.gpn.etranintegration.service.util.DateUtils;
import java.time.LocalDateTime;

@Slf4j
@Service
class IbpdServiceImpl implements IbpdService {

    private static final String KEY_FOR_INVOICE_MESSAGE = "inv_info";

    @Value("${service.ibpd.uri.lastOperDate}")
    private String lastOperDateUri;

    @Value("${service.ibpd.uri.updateInvoice}")
    private String updateInvoiceUri;

    @Value("${service.ibpd.uri.newInvoice}")
    private String newInvoiceUri;

    @Value("${service.ibpd.requestCnt}")
    private int countRequest;

    private final RestTemplate restTemplate;

    public IbpdServiceImpl(@Qualifier("restIbpd") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public LocalDateTime getLastOperDateByInvoiceId(String invNumber) {
        ResponseEntity<LastOperDateResponse[]> responseEntity;
        final String url = lastOperDateUri + invNumber;
        log.info("Request last operation date to IBPD with invoice number: {}", invNumber);
        for (int i = 0; i < countRequest; i++) {
            try {
                responseEntity = restTemplate.getForEntity(url, LastOperDateResponse[].class);
            } catch (RestClientException e) {
                log.error("IBPD request error.", e);
                continue;
            }
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("IBPD received HTTP code is not success. Code: {}", responseEntity.getStatusCode());
                continue;
            }
            if (hasLastOperDateInResponse(responseEntity)) {
                String lastOperDate = responseEntity.getBody()[0].getLastOperDate();
                log.info("Response from IBPD received. Last operation date: {}", lastOperDate);
                return DateUtils.convertToLocalDateTime(lastOperDate);
            }
        }
        return null;
    }

    @Override
    public void setNewInvoice(InvoiceResponse invoiceFromEtran) {
        sendInvoice(invoiceFromEtran, newInvoiceUri);
    }

    @Override
    public void updateInvoice(InvoiceResponse invoiceFromEtran) {
        sendInvoice(invoiceFromEtran, updateInvoiceUri);
    }

    private void sendInvoice(InvoiceResponse invoiceFromEtran, String invoiceUri) {
        ResponseEntity<Void> responseEntity;
        JSONObject request = new JSONObject();
        request.put(KEY_FOR_INVOICE_MESSAGE, invoiceFromEtran.getMessage());
        log.info("Set invoice to IBPD with invoiceId: {}. Message: {}", invoiceFromEtran.getInvoiceId(), request);
        for (int i = 0; i < countRequest; i++) {
            try {
                responseEntity = restTemplate.postForEntity(invoiceUri, request.toString(), Void.class);
            } catch (RestClientException e) {
                log.error("IBPD request error.", e);
                continue;
            }
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("IBPD received HTTP code is not Success. Code: {}", responseEntity.getStatusCode());
                continue;
            }
            return;
        }
    }

    private static boolean hasLastOperDateInResponse(ResponseEntity<LastOperDateResponse[]> responseEntity) {
        return responseEntity.getBody() != null
                && responseEntity.getBody().length != 0
                && responseEntity.getBody()[0] != null
                && responseEntity.getBody()[0].getLastOperDate() != null
                && !responseEntity.getBody()[0].getLastOperDate().isEmpty();
    }
}
