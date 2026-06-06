package ru.finuniv.retailshield.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Форма обратной связи: оставить телефон/имя для звонка менеджера.
 * Используется на экране отказа (индивидуальное рассмотрение) и на экране
 * результата оценки.
 *
 * В прототипе данные формы только логируются. В production-системе они
 * отправлялись бы в CRM страховщика.
 */
public class ContactForm {

    @NotBlank(message = "Укажите имя контактного лица")
    private String name;

    @NotBlank(message = "Укажите номер телефона")
    @Pattern(regexp = "^\\+?[0-9 ()\\-]{7,20}$",
            message = "Номер телефона должен содержать только цифры, пробелы, скобки и дефисы")
    private String phone;

    private String companyName;
    private String comment;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}