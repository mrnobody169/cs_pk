package com.pokertrack.mapper;

import com.pokertrack.dto.response.DayReportRow;
import com.pokertrack.dto.response.MemberProfitRow;
import com.pokertrack.dto.response.PlayerReportRow;
import com.pokertrack.dto.response.ReportSummaryResponse;
import com.pokertrack.dto.response.TransactionRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReportMapper {
        ReportSummaryResponse getSummary(
                        @Param("groupId") Long groupId,
                        @Param("from") LocalDate from,
                        @Param("to") LocalDate to);

        List<PlayerReportRow> getByPlayer(
                        @Param("groupId") Long groupId,
                        @Param("from") LocalDate from,
                        @Param("to") LocalDate to);

        List<DayReportRow> getByDay(
                        @Param("groupId") Long groupId,
                        @Param("from") LocalDate from,
                        @Param("to") LocalDate to);

        /** Dashboard: profit per member for a single group */
        List<MemberProfitRow> getMemberProfitByGroup(
                        @Param("groupId") Long groupId,
                        @Param("from") LocalDate from,
                        @Param("to") LocalDate to);

        /** Dashboard: profit per member across all groups the user belongs to */
        List<MemberProfitRow> getMemberProfitAllGroups(
                        @Param("userId") Long userId,
                        @Param("from") LocalDate from,
                        @Param("to") LocalDate to);

        /** Transaction log for a single group */
        List<TransactionRow> getTransactionsByGroup(
                        @Param("groupId") Long groupId,
                        @Param("from") LocalDate from,
                        @Param("to") LocalDate to);

        /** Transaction log across all groups the user belongs to */
        List<TransactionRow> getTransactionsAllGroups(
                        @Param("userId") Long userId,
                        @Param("from") LocalDate from,
                        @Param("to") LocalDate to);
}
