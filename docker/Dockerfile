FROM ubuntu:20.04
ENV DEBIAN_FRONTEND=noninteractive
RUN dpkg --add-architecture i386
RUN apt-get -y update
RUN apt-get -y upgrade
RUN apt-get -y install wget gnupg2 software-properties-common
RUN wget -nc https://dl.winehq.org/wine-builds/winehq.key && apt-key add winehq.key && rm winehq.key
RUN apt-add-repository 'deb https://dl.winehq.org/wine-builds/ubuntu/ eoan main'
RUN apt-get -y install --install-recommends winehq-staging
RUN wine reg add "HKLM\\System\\CurrentControlSet\\Control\\Session Manager\\Environment" /v PATH /t REG_EXPAND_SZ /d "C:\\jdk-11.0.5+10\\bin;C:\\windows\\system32;C:\\windows;C:\\windows\\system32\\wbem" /f && wineserver -k
RUN apt-get -y install less
COPY gradlew* /root/
COPY ./gradle /root/gradle
RUN wine cmd /c set path
RUN apt-get -y install unzip
RUN (cd /tmp && wget -q https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.5%2B10/OpenJDK11U-jdk_x86-32_windows_hotspot_11.0.5_10.zip && cd /root/.wine/drive_c && unzip /tmp/OpenJDK11U-jdk_x86-32_windows_hotspot_11.0.5_10.zip && rm /tmp/OpenJDK11U-jdk_x86-32_windows_hotspot_11.0.5_10.zip)
RUN (cd /root && script -c 'wine cmd /c gradlew.bat' /dev/null </dev/null)
RUN apt-get -y install net-tools curl
RUN (apt-get install -y -q innoextract && \
	cd /tmp && \
	wget -nc -O is.exe http://files.jrsoftware.org/is/5/innosetup-5.6.1.exe && \
	innoextract is.exe && \
	mv app /root/.wine/drive_c/innosetup && \
	rm -rf is.exe app)
RUN ( \
	wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | apt-key add - && \
	add-apt-repository --yes https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/ && \
	apt-get update && \
	apt-get install -y adoptopenjdk-11-hotspot && \
	true)
RUN (cd /root && ./gradlew)
RUN apt-get install -y graphicsmagick-imagemagick-compat
