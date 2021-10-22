package ru.gpn.etranintegration.model.etran.message.invoiceStatus;

import lombok.Data;
import ru.gpn.etranintegration.model.etran.message.ValueAttribute;

import javax.xml.bind.annotation.*;
import java.util.List;

@Data
@XmlRootElement(name = "invoiceStatusReply")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"invoice", "operId", "operDate", "warning"})
public class InvoiceStatusResponse {

    private List<Invoice> invoice;

    @XmlElement(name = "OperId")
    private ValueAttribute operId;

    @XmlElement(name = "OperDate")
    private ValueAttribute operDate;

    @XmlElement
    private ValueAttribute warning;

    private boolean errorAuth;

}