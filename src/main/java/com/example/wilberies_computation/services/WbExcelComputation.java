package com.example.wilberies_computation.services;

import com.example.wilberies_computation.dtos.wilberies.AdditionalPaymentsRowDto;
import com.example.wilberies_computation.dtos.wilberies.ReportComputeResultDto;
import com.example.wilberies_computation.dtos.wilberies.ReportRowDeliveryDto;
import com.example.wilberies_computation.dtos.wilberies.ReportRowFineDto;
import com.example.wilberies_computation.dtos.wilberies.ReportRowSaleDto;
import com.example.wilberies_computation.entity.wildberies.ProductEntity;
import com.example.wilberies_computation.entity.wildberies.ReportWbEntity;
import com.example.wilberies_computation.entity.wildberies.embded.ReportProductInfoWbEmbded;
import com.example.wilberies_computation.repositories.ProductRepository;
import com.example.wilberies_computation.repositories.ReportRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class WbExcelComputation implements Computation {

  @Autowired
  private ReportRepository reportRepository;
  @Autowired
  private ProductRepository productRepository;
//
//  public List<ReportComputeResultDto> computeAndSaveList(List<MultipartFile> xlsxs) {
//    List<ReportComputeResultDto> resultDtos = new ArrayList<>();
//    xlsxs.forEach(xlsx -> resultDtos.add(this.computeAndSave(xlsx)));
//    return resultDtos;
//  }

  @SneakyThrows
  public ReportComputeResultDto computeAndSave(MultipartFile xlsx) {
    Workbook workbook = new XSSFWorkbook(xlsx.getInputStream());
    Sheet sheet = workbook.getSheetAt(0);
    List<ReportRowSaleDto> rowsSaleList = excelSheetToListSell(sheet);
    List<ReportRowDeliveryDto> rowsDeliveryList = excelSheetToListDelivery(sheet);
    List<ReportRowFineDto> rowsFineList = excelSheetToListFine(sheet);
    List<AdditionalPaymentsRowDto> additionalPaymentsList = excelSheetToListAdditionalPayments(
        sheet);
    this.createReport(rowsSaleList, rowsDeliveryList, rowsFineList, additionalPaymentsList);
    return ReportComputeResultDto.builder().build();

  }

  private List<ReportRowSaleDto> excelSheetToListSell(Sheet sheet) {
    DataFormatter formatter = new DataFormatter();
    List<ReportRowSaleDto> rowsList = new ArrayList<>();
    for (Row row : sheet) {
      if (row.getRowNum() == 0) {
        continue;
      }
      if (formatter.formatCellValue(row.getCell(10)).equalsIgnoreCase("Продажа")) {
        System.out.println(row.getCell(10));
        rowsList.add(ReportRowSaleDto.builder()
            .number(formatter.formatCellValue(row.getCell(0)))
            .vendorСode(formatter.formatCellValue(row.getCell(5)))
            .name(formatter.formatCellValue(row.getCell(6)))
            .orderDate(LocalDate.parse(formatter.formatCellValue(row.getCell(11))))
            .sellDate(LocalDate.parse(formatter.formatCellValue(row.getCell(12))))
            .sellingPrice(BigDecimal.valueOf(row.getCell(15).getNumericCellValue()))
            .tax(BigDecimal.valueOf(row.getCell(15).getNumericCellValue())
                .multiply(BigDecimal.valueOf(0.07d)))
            .toMoneyTransfer(BigDecimal.valueOf(row.getCell(31).getNumericCellValue()))
            .build());
      }
    }
    return rowsList;
  }

  private List<ReportRowDeliveryDto> excelSheetToListDelivery(Sheet sheet) {
    DataFormatter formatter = new DataFormatter();
    List<ReportRowDeliveryDto> rowsList = new ArrayList<>();
    for (Row row : sheet) {
      if (row.getRowNum() == 0) {
        continue;
      }
      if (formatter.formatCellValue(row.getCell(10)).equalsIgnoreCase("Логистика")
          || formatter.formatCellValue(row.getCell(10)).equalsIgnoreCase("Логистика сторно")) {
        rowsList.add(ReportRowDeliveryDto.builder()
            .number(formatter.formatCellValue(row.getCell(0)))
            .vendorСode(formatter.formatCellValue(row.getCell(5)))
            .name(formatter.formatCellValue(row.getCell(6)))
            .orderDate(LocalDate.parse(formatter.formatCellValue(row.getCell(11))))
            .saleDate(LocalDate.parse(formatter.formatCellValue(row.getCell(12))))
            .toMoneyPaid(BigDecimal.valueOf(row.getCell(34).getNumericCellValue()))
            .build());
      }
    }
    return rowsList;
  }

  private List<ReportRowFineDto> excelSheetToListFine(Sheet sheet) {
    DataFormatter formatter = new DataFormatter();
    List<ReportRowFineDto> rowsList = new ArrayList<>();
    for (Row row : sheet) {
      if (row.getRowNum() == 0) {
        continue;
      }
      if (formatter.formatCellValue(row.getCell(10)).equalsIgnoreCase("Штраф")) {
        rowsList.add(ReportRowFineDto.builder()
            .number(formatter.formatCellValue(row.getCell(0)))
            .vendorCode(formatter.formatCellValue(row.getCell(5)))
            .name(formatter.formatCellValue(row.getCell(6)))
            .orderDate(LocalDate.parse(formatter.formatCellValue(row.getCell(11))))
            .saleDate(LocalDate.parse(formatter.formatCellValue(row.getCell(12))))
            .finesPaid(BigDecimal.valueOf(row.getCell(35).getNumericCellValue()))
            .build());
      }
    }
    return rowsList;
  }

  private List<AdditionalPaymentsRowDto> excelSheetToListAdditionalPayments(Sheet sheet) {
    DataFormatter formatter = new DataFormatter();
    List<AdditionalPaymentsRowDto> rowsList = new ArrayList<>();
    for (Row row : sheet) {
      if (row.getRowNum() == 0) {
        continue;
      }
      if (formatter.formatCellValue(row.getCell(10)).equalsIgnoreCase("Оплата брака")) {
        rowsList.add(AdditionalPaymentsRowDto.builder()
            .paid(BigDecimal.valueOf(row.getCell(29).getNumericCellValue()))
            .build());
      }
    }
    return rowsList;
  }

  private ReportWbEntity createReport(List<ReportRowSaleDto> saleRows,
      List<ReportRowDeliveryDto> deliveryRows, List<ReportRowFineDto> rowsFineList,
      List<AdditionalPaymentsRowDto> additionalPaymentsRows) {

    List<ReportProductInfoWbEmbded> productInfoEmbdeds = saleRowsToProductInfoEmbded(saleRows);
    BigDecimal addPayments = additionalPaymentsRows.stream().map(AdditionalPaymentsRowDto::getPaid)
        .reduce((BigDecimal::add)).orElse(BigDecimal.ZERO);
    BigDecimal howPaid = saleRows.stream().map(ReportRowSaleDto::getToMoneyTransfer)
        .reduce((BigDecimal::add)).orElse(BigDecimal.ZERO);
    BigDecimal deliveryPaid = deliveryRows.stream().map(ReportRowDeliveryDto::getToMoneyPaid)
        .reduce((BigDecimal::add)).orElse(BigDecimal.ZERO);
    BigDecimal revenue = howPaid.subtract(deliveryPaid).add(addPayments);
    BigDecimal primeCost = productInfoEmbdeds.stream().map(ReportProductInfoWbEmbded::getPrimeCost)
        .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

    BigDecimal fines = rowsFineList.stream().map(ReportRowFineDto::getFinesPaid)
        .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

    productInfoEmbdeds.stream().forEach(System.out::println);
    ReportWbEntity reportEntity = ReportWbEntity.builder()
        .howPaid(howPaid)
        .dateFrom(saleRows.stream().map(ReportRowSaleDto::getSellDate)
            .min(Comparator.comparing(LocalDate::toEpochDay)).orElse(null))
        .dateTo(saleRows.stream().map(ReportRowSaleDto::getSellDate)
            .max(Comparator.comparing(LocalDate::toEpochDay)).orElse(null))
        .deliveryPaid(deliveryPaid)
        .revenue(revenue)
        .quantitySold((int) saleRows.stream().count())
        .productInfos(productInfoEmbdeds)
        .primeCost(primeCost)
        .netProfit(revenue.subtract(primeCost).subtract(fines))
        .fines(fines)
        .build();

    return reportRepository.save(reportEntity);
  }

  private List<ReportProductInfoWbEmbded> saleRowsToProductInfoEmbded(
      List<ReportRowSaleDto> saleRows) {
    List<ReportProductInfoWbEmbded> productInfoEmbdeds = new ArrayList<>();
    Set<String> vendorCodes = Set.copyOf(
        saleRows.stream().map(ReportRowSaleDto::getVendorСode).collect(
            Collectors.toList()));

    for (String vendorCode : vendorCodes) {
      ReportProductInfoWbEmbded infoEmbded = new ReportProductInfoWbEmbded();
      System.out.println("---------------------------------");
      System.out.println(vendorCode);
      System.out.println("---------------------------------");
      ProductEntity product = productRepository.getByVendorCodeIgnoreCase(vendorCode).orElse(null);
      System.out.println(vendorCode);
      for (ReportRowSaleDto rowSale : saleRows) {
        if (rowSale.getVendorСode().equals(vendorCode)) {
          if (infoEmbded.getName() == null) {
            infoEmbded.setName(rowSale.getName());
          }
          if (infoEmbded.getProduct() == null) {
            infoEmbded.setProduct(product);
          }
          infoEmbded.setHowManySold(infoEmbded.getHowManySold() + 1L);
          infoEmbded.setAccrued(infoEmbded.getAccrued().add(rowSale.getToMoneyTransfer()));
          infoEmbded.setTax(infoEmbded.getTax().add(rowSale.getTax()));
          infoEmbded.setAccruedWitchTax(infoEmbded.getAccrued().subtract(infoEmbded.getTax()));
          infoEmbded.setNetProfit(infoEmbded.getAccruedWitchTax()
              .subtract(BigDecimal.valueOf(infoEmbded.getHowManySold()).multiply(
                  product == null ? BigDecimal.ONE
                      : product.getPrimeCost() == null ? BigDecimal.ZERO
                          : product.getPrimeCost())));
          infoEmbded.setPrimeCost(infoEmbded.getPrimeCost().add(
              product == null ? BigDecimal.ZERO
                  : product.getPrimeCost() == null ? BigDecimal.ZERO
                      : product.getPrimeCost()));
          infoEmbded.setFullName(product.getFullName());
        }
      }
      productInfoEmbdeds.add(infoEmbded);
    }

    return productInfoEmbdeds;
  }
}

