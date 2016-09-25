#ifndef __CWAPPER_HPP
#define __CWAPPER_HPP

#include <cppcms/application.h>
#include <cppcms/applications_pool.h>
#include <cppcms/service.h>
#include <cppcms/http_response.h>
#include <cppcms/http_request.h>
#include <cppcms/url_mapper.h>
#include <cppcms/url_dispatcher.h>

#include <iostream>
#include <vector>

#include <boost/algorithm/string.hpp>

#include <boost/variant.hpp>
#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/json_parser.hpp>
#include <boost/property_tree/xml_parser.hpp>
#include <boost/property_tree/info_parser.hpp>
#include <boost/property_tree/exceptions.hpp>
#include <boost/lexical_cast.hpp>

namespace cwapper {
    typedef boost::variant<
        std::string,
        std::vector<std::string>
    > parameter;

    class error: std::exception {};
}

std::ostream& operator<<(std::ostream&, const boost::property_tree::ptree&);

template <typename T>
std::ostream& operator<<(std::ostream& out, const std::vector<T>& v) {
    for(const T& e: v) {
        out << e << ", ";
    }
    
    return out;
}

#endif
