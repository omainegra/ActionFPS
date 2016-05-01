package com.actionfps.stats

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import scala.xml.Elem

/**
  * Created by me on 22/04/2016.
  */
object Stats {

  def idsToTableRows(ids: List[String], width: Double, height: Double, r: Double): List[Elem] = {
    implicit val ordering = Ordering.by[ZonedDateTime, Double](_.toEpochSecond)
    val horizontalPadding = r
    ids.map(ZonedDateTime.parse)
      .groupBy(_.withHour(0).withMinute(0).withSecond(0).withNano(0))
      .toList.sortBy(_._1).reverse
      .map { case (header, items) =>
        <tr>
          <th>
            {header.format(DateTimeFormatter.ofPattern("d MMM yy"))}
          </th>
          <td>
            <svg width={s"${width + 2 * horizontalPadding}"} height={s"$height"}>
              <g>
                {items.map { item =>
                val cx = width * (item.toEpochSecond - header.toEpochSecond) / (24 * 3600)
                <a xlink:href={s"/game/?id=$item"}
                   xhref:title={s"Game $item"}>
                  <circle
                  cy={s"${height / 2}"} r={s"$r"}
                  cx={s"${cx + horizontalPadding}"}/>
                </a>
              }}
              </g>
            </svg>
          </td>
        </tr>
      }
  }

}
