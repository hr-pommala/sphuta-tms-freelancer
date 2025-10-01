//package net.sphuta.tms.freelancer.dto;
//
//import jakarta.validation.constraints.NotBlank;
//
///**
// * Optional DTO if you want to receive ack token in a request body.
// * The controller in this project uses query param 't' for ack token,
// * but this record is provided if you prefer body-based ack.
// */
//public record AckRequest(
//        @NotBlank(message = "t (token) is required")
//        String t
//) {}
