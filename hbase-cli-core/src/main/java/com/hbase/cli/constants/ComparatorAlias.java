package com.hbase.cli.constants;

public enum ComparatorAlias {

	endswith, 
	startswith {
		public String toString() {
			return "binaryprefix";
		}
	},
	contains {
		public String toString() {
			return "substring";
		}
	},
	regex {
		public String toString() {
			return "regexstring";
		}
	},
	exact {
		public String toString() {
			return "binary";
		}
	}
}
