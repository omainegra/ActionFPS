/**
  * Created by William on 07/12/2015.
  */
import javax.inject.Inject

import play.api.http.HttpFilters
import play.filters.cors.CORSFilter
import play.filters.gzip.GzipFilter

class Filters @Inject() (gzipFilter: GzipFilter) extends HttpFilters {
  def filters = Seq(gzipFilter)
}
