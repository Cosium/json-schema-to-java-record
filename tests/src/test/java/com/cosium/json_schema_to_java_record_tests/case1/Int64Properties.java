package com.cosium.json_schema_to_java_record_tests.case1;

import java.util.function.Function;

/**
 * Fails to compile if an int64 property is not generated as a long: an int would box to Integer.
 */
class Int64Properties {

  static final Function<Customer, Long> REQUIRED = Customer::id;
  static final Function<Customer, Long> NON_REQUIRED = Customer::referrerId;
}
