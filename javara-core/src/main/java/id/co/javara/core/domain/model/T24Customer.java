package id.co.javara.core.domain.model;

import id.co.javara.core.domain.vo.CustomerId;

public record T24Customer(
    CustomerId customerId,
    String cifNumber,
    String fullName,
    String idType,
    String idNumber,
    String branchCode,
    String status,
    String address,
    String phone,
    String email
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CustomerId customerId;
        private String cifNumber;
        private String fullName;
        private String idType;
        private String idNumber;
        private String branchCode;
        private String status;
        private String address;
        private String phone;
        private String email;

        public Builder customerId(CustomerId id) { this.customerId = id; return this; }
        public Builder cifNumber(String cif) { this.cifNumber = cif; return this; }
        public Builder fullName(String name) { this.fullName = name; return this; }
        public Builder idType(String type) { this.idType = type; return this; }
        public Builder idNumber(String num) { this.idNumber = num; return this; }
        public Builder branchCode(String bc) { this.branchCode = bc; return this; }
        public Builder status(String s) { this.status = s; return this; }
        public Builder address(String addr) { this.address = addr; return this; }
        public Builder phone(String p) { this.phone = p; return this; }
        public Builder email(String e) { this.email = e; return this; }

        public T24Customer build() {
            if (customerId == null) throw new IllegalStateException("customerId is required");
            if (cifNumber == null || cifNumber.isBlank()) throw new IllegalStateException("cifNumber is required");
            if (fullName == null || fullName.isBlank()) throw new IllegalStateException("fullName is required");
            return new T24Customer(
                customerId, cifNumber, fullName, idType, idNumber,
                branchCode, status, address, phone, email
            );
        }
    }
}
