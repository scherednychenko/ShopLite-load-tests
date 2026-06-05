# Apache JMeter image for the ShopLite load test.
# Includes the BlazeMeter "Random CSV Data Set" plugin that the test plan uses
# (testclass com.blazemeter.jmeter.RandomCSVDataSetConfig).
FROM eclipse-temurin:17-jre-jammy

ARG JMETER_VERSION=5.5
ENV JMETER_HOME=/opt/apache-jmeter-${JMETER_VERSION}
ENV PATH=${JMETER_HOME}/bin:${PATH}

RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends wget ca-certificates; \
    rm -rf /var/lib/apt/lists/*; \
    # Apache JMeter
    wget -q "https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz" -O /tmp/jmeter.tgz; \
    tar -xzf /tmp/jmeter.tgz -C /opt; \
    rm /tmp/jmeter.tgz; \
    # JMeter Plugins Manager + cmdrunner, then install the Random CSV Data Set plugin
    wget -q "https://repo1.maven.org/maven2/kg/apc/jmeter-plugins-manager/1.10/jmeter-plugins-manager-1.10.jar" -O "${JMETER_HOME}/lib/ext/jmeter-plugins-manager.jar"; \
    wget -q "https://repo1.maven.org/maven2/kg/apc/cmdrunner/2.3/cmdrunner-2.3.jar" -O "${JMETER_HOME}/lib/cmdrunner-2.3.jar"; \
    java -cp "${JMETER_HOME}/lib/ext/jmeter-plugins-manager.jar" org.jmeterplugins.repository.PluginManagerCMDInstaller; \
    "${JMETER_HOME}/bin/PluginsManagerCMD.sh" install bzm-random-csv

# Test assets (test plan, config, data, groovy). Paths inside the plan are
# relative to the working dir, so keep the same jmeter/ layout and run from /test.
WORKDIR /test
COPY jmeter ./jmeter

ENTRYPOINT ["jmeter"]
