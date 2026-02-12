package ru.clouddonate.cloudpayments.service;

public interface Service {

    default void enable() {}
    default void reload() {}
    default void disable() {}
}
