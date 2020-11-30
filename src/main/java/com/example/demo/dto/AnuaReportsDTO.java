package com.example.demo.dto;

import java.util.List;

import lombok.Data;

@Data

public class AnuaReportsDTO {

	private String symbol;
	private List<ReportItemDTO> annualReports;
}