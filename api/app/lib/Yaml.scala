package lib

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

object Yaml {

  private val yamlMapper = new ObjectMapper(new YAMLFactory())
  private val jsonMapper = new ObjectMapper()

  def toJson(yaml: String): String = jsonMapper.writeValueAsString(yamlMapper.readTree(yaml))

}
