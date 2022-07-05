cmake_minimum_required(VERSION 3.22.1)
project(xposedmod)

enable_language(CXX C)
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_C_STANDARD 11)

find_library(ANDROID_LIBS log)

option(DEX_BUILDER_BUILD_SHARED "" OFF)
option(ABSL_PROPAGATE_CXX_STD "" ON)

add_subdirectory(dex_builder)

add_library(xposedmod SHARED xposedmod.cc)

set_target_properties(xposedmod PROPERTIES
        CXX_EXTENSIONS OFF
        POSITION_INDEPENDENT_CODE ON
        )

target_link_libraries(xposedmod ${ANDROID_LIBS} dex_builder_static)