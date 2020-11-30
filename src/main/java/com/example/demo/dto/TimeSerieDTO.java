package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data

public class TimeSerieDTO {
	@JsonProperty("2020-11-25 20:00:00")
	private TimeItemDTO item;

}
