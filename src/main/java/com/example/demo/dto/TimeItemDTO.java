package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data

public class TimeItemDTO {
	@JsonProperty("1. open")
	private double open;
	@JsonProperty("2. high")
	private double high;
	@JsonProperty("3. low")
	private double low;
	@JsonProperty("4. close")
	private long close;
	@JsonProperty("5. volume")
	private long volume;

}
