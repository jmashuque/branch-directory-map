cmake_minimum_required(VERSION 4.1.2)

project("native-lib" VERSION 1.0.0)

if(DEFINED HEXAGON)
    add_compile_definitions(DATA="${HEXAGON}")
else()
    message(WARNING "HEXAGON is not defined")
endif()

add_library( native-lib
        SHARED
        native-lib.cpp )

find_library( log-lib
        log )

target_link_libraries( native-lib
        ${log-lib} )
