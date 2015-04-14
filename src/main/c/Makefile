#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#
# Top-level Makefile for TCNATIVE
#

CFLAGS = -g -O2 -DHAVE_OPENSSL
CPPFLAGS = -I/usr/local/opt/openssl/include 

# gets substituted into some targets
TCNATIVE_MAJOR_VERSION=1
TCNATIVE_DOTTED_VERSION=1.1.32
TCNATIVE_LIBTOOL_VERSION=1:32:1

srcdir = .


# this sucks, but it's the only way to add extra flags to the LT_COMPILE
INCLUDES = -g -O2 -DHAVE_OPENSSL -I/usr/local/opt/openssl/include   -I/Users/leo/Documents/uni/master/jboss/tomcat-openssl/src/main/c/include -I/Library/Java/JavaVirtualMachines/jdk1.8.0_25.jdk/Contents/Home/include -I/Library/Java/JavaVirtualMachines/jdk1.8.0_25.jdk/Contents/Home/include/darwin -I/usr/local/Cellar/openssl/1.0.2a-1//include  -I/usr/local/Cellar/apr/1.5.1/libexec/include/apr-1 
TCNATIVE_LDFLAGS = -L/usr/local/Cellar/openssl/1.0.2a-1//lib -lssl -lcrypto -L/usr/local/opt/openssl/lib
TCNATIVE_LIBS =  /usr/local/Cellar/apr/1.5.1/libexec/lib/libapr-1.la -lpthread

TARGET_LIB = libtcnative-${TCNATIVE_MAJOR_VERSION}.la
EXTRA_OS_LINK=$(TCNATIVE_LDFLAGS) $(TCNATIVE_LIBS)
TCNATIVE_PCFILE = tcnative-$(TCNATIVE_MAJOR_VERSION).pc
INSTALL = /usr/bin/install -c
INSTALL_DATA = ${INSTALL} -m 644

TARGETS = $(TARGET_LIB)

# bring in rules.mk for standard functionality
include /Users/leo/Documents/uni/master/jboss/tomcat-openssl/src/main/c/build/rules.mk
include /Users/leo/Documents/uni/master/jboss/tomcat-openssl/src/main/c/build-outputs.mk

LINK          = $(LIBTOOL) $(LTFLAGS) --mode=link $(LT_LDFLAGS) $(COMPILE) -version-info $(TCNATIVE_LIBTOOL_VERSION) $(ALL_LDFLAGS) -o $@
CLEAN_SUBDIRS = test

CLEAN_TARGETS = .make.dirs
DISTCLEAN_TARGETS = config.cache config.log config.status libtool \
	build/rules.mk tcnative.pc
EXTRACLEAN_TARGETS = configure aclocal.m4 build-outputs.mk \
	build/apr_common.m4 build/find_apr.m4 build/install.sh \
	build/config.guess build/config.sub tcnative.spec

prefix=/usr/local/apr
exec_prefix=${prefix}
bindir=${exec_prefix}/bin
libdir=${exec_prefix}/lib
includedir=${prefix}/include/apr-${TCNATIVE_MAJOR_VERSION}
top_srcdir=/Users/leo/Documents/uni/master/jboss/tomcat-openssl/src/main/c
top_blddir=/Users/leo/Documents/uni/master/jboss/tomcat-openssl/src/main/c


install: $(TARGET_LIB)
	$(APR_MKDIR) $(DESTDIR)$(includedir) $(DESTDIR)$(libdir)/pkgconfig \
		     $(DESTDIR)$(libdir) $(DESTDIR)$(bindir)
	$(INSTALL_DATA) tcnative.pc $(DESTDIR)$(libdir)/pkgconfig/$(TCNATIVE_PCFILE)
	list='$(INSTALL_SUBDIRS)'; for i in $$list; do \
		( cd $$i ; $(MAKE) DESTDIR=$(DESTDIR) install ); \
	done
	$(LIBTOOL) --mode=install $(INSTALL) -m 755 $(TARGET_LIB) $(DESTDIR)$(libdir)

$(TARGET_LIB): $(OBJECTS)
	$(LINK) -rpath $(libdir) $(OBJECTS) -L/usr/local/Cellar/openssl/1.0.2a-1//lib -lssl -lcrypto -L/usr/local/opt/openssl/lib  /usr/local/Cellar/apr/1.5.1/libexec/lib/libapr-1.la -lpthread

check: $(TARGET_LIB)
	(cd test && $(MAKE) check)
