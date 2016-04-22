package com.actionfps.stats

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import scala.xml.Elem

/**
  * Created by me on 22/04/2016.
  */
object Stats {

  def idsToTableRows(ids: List[String]): List[Elem] = {
    ids.map(ZonedDateTime.parse)
      .groupBy(_.withHour(0).withMinute(0).withSecond(0).withNano(0))
      .toList.sortBy(_._1).reverse
      .map { case (header, items) =>
        <tr>
          <th>
            {header.format(DateTimeFormatter.ofPattern("e MMM"))}
          </th>
          <td>
            <svg width="900" height="20">
              <g>
                {items.map { item =>
                val cx = 900f * (item.toEpochSecond - header.toEpochSecond) / (1000 * 24 * 3600)
                  <circle cy="10" r="5" cx={s"$cx"}/>
              }}
              </g>
            </svg>
          </td>
        </tr>
      }
  }

  def main(args: Array[String]): Unit = {
    idsToTableRows(
      ids = scala.io.Source.fromInputStream(System.in).getLines().map { line =>
        if (line.contains("\t")) line.substring(0, line.indexOf("\t"))
        else line
      }.toList
    )
  }

}
