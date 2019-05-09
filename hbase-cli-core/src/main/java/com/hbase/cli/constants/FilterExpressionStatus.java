package com.hbase.cli.constants;

public enum FilterExpressionStatus {

	PASS {
		public String toString() {
			return "pass";
		}
	},

	NOTPASS {
		public String toString() {
			return "notpass";
		}
	},

	FAIL {
		public String toString() {
			return "fail";
		}
	}

}
